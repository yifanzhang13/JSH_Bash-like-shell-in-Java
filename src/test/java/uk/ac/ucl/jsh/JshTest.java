package uk.ac.ucl.jsh;

import org.junit.*;
import org.junit.rules.ExpectedException;
import uk.ac.ucl.jsh.app.*;
import uk.ac.ucl.jsh.core.*;
import uk.ac.ucl.jsh.core.parser.*;
import uk.ac.ucl.jsh.utility.IntelligentPath;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

public class JshTest {
    private Jsh jsh;
    private static File dir1, dir2, dir3, aiml, btxt, dotc;
    private PipedOutputStream out;
    private PipedInputStream in;
    private BufferedReader reader;

    @BeforeClass
    public static void beforeClass() throws Exception {
        Jsh jsh = new Jsh();

        dir1 = IntelligentPath.getPath("dir1", jsh.getJshCore().getCurrentDirectory()).toFile();
        dir2 = IntelligentPath.getPath("dir2", jsh.getJshCore().getCurrentDirectory()).toFile();
        dir1.mkdir();
        dir2.mkdir();

        dir3 = IntelligentPath.getPath("dir3", dir1.toPath()).toFile();
        dir3.mkdir();

        aiml = IntelligentPath.getPath("a.iml", dir1.toPath()).toFile();
        aiml.createNewFile();

        btxt = IntelligentPath.getPath("b.txt", dir1.toPath()).toFile();
        btxt.createNewFile();

        dotc = IntelligentPath.getPath(".c", dir1.toPath()).toFile();
        dotc.createNewFile();

        FileOutputStream aimlOutput = new FileOutputStream(aiml);
        PrintStream printStream = new PrintStream(aimlOutput);
        printStream.println("hello");
        printStream.println("world!");

        printStream.close();
        aimlOutput.close();
    }


    @Before
    public void initialize() throws Exception {
        jsh = new Jsh();
        in = new PipedInputStream(2048);
        out = new PipedOutputStream(in);
        reader = new BufferedReader(new InputStreamReader(in));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testCommandException2() {
        Command command = new Command("cat");
        command.addArgument(new Argument("a.txt"));
        command.addArgument(new Argument("<"));

        assertThrows(RuntimeException.class, () -> Command.preprocessCommand(command));
    }

    @Test
    public void testCommandException3() {
        Command command = new Command("cat");
        command.addArgument(new Argument(">"));
        command.addArgument(new Argument("a.txt"));
        command.addArgument(new Argument(">"));
        command.addArgument(new Argument("a.txt"));


        assertThrows(RuntimeException.class, () -> Command.preprocessCommand(command));
    }

    @Test
    public void testCommandException4() {
        Command command = new Command("cat");
        command.addArgument(new Argument("a.txt"));
        command.addArgument(new Argument(">"));
        command.addArgument(new Argument("b.txt"));
        command.addArgument(new Argument("<"));
        command.addArgument(new Argument("c.txt"));

        Command.preprocessCommand(command);
        assertEquals(command.getInputRedirect().getArgumentString(), "c.txt");
        assertEquals(command.getOutputRedirect().getArgumentString(), "b.txt");
    }

    @Test
    public void testCommandRedirect() {
        Command command = new Command("cat");
        command.addArgument(new Argument(">a.txt"));

        Command.preprocessCommand(command);
        assertEquals(command.getOutputRedirect().getArgumentString(), "a.txt");
    }

    @Test
    public void testParseBackQuote() {
        Parser parser = new Parser();
        assertThrows(IllegalArgumentException.class, () -> parser.parse("`sdf\n`"));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("`sdf"));
    }

    @Test
    public void testParseSingleQuote() {
        Parser parser = new Parser();
        assertThrows(IllegalArgumentException.class, () -> parser.parse("echo 'w\n'"));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("echo 'sdf"));
    }

    @Test
    public void testParseDoubleQuote() {
        Parser parser = new Parser();
        assertThrows(IllegalArgumentException.class, () -> parser.parse("echo \"as\n\""));
        assertThrows(IllegalArgumentException.class, () -> parser.parse("echo \"as"));
    }

    @Test
    public void testParseArgument() {
        Parser parser = new Parser();
        assertThrows(IllegalArgumentException.class, () -> parser.parse("echo nih\nao"));
    }

    @Test
    public void testIntelligentPath() {
        Path homeDirectory = Paths.get(System.getProperty("user.home"));

        assertEquals(homeDirectory.toString(), IntelligentPath.getPath("~", jsh.getJshCore().getCurrentDirectory()).toString());
        assertEquals(jsh.getJshCore().getCurrentDirectory().toString(), IntelligentPath.getPath(".", jsh.getJshCore().getCurrentDirectory()).toString());
        assertEquals(jsh.getJshCore().getCurrentDirectory().getParent().toString(), IntelligentPath.getPath("..", jsh.getJshCore().getCurrentDirectory()).toString());
        assertEquals(jsh.getJshCore().getCurrentDirectory().toAbsolutePath().toString() + "/" + "a.txt", IntelligentPath.getPath("./a.txt", jsh.getJshCore().getCurrentDirectory()).toString());
        assertEquals("/" + "a.txt", IntelligentPath.getPath("/a.txt", jsh.getJshCore().getCurrentDirectory()).toString());
        assertEquals(jsh.getJshCore().getCurrentDirectory().toString() + "/" + "a.txt", IntelligentPath.getPath("../a.txt", IntelligentPath.getPath("dir1", jsh.getJshCore().getCurrentDirectory())).toString());
    }

    @Test
    public void testAbstractApp() throws Exception {
        App app = FactoryProvider.getAppFactory().create("echo");
        Method method = AbstractApp.class.getDeclaredMethod("writeOutputStream", String.class);
        method.setAccessible(true);
        assertThrows(InvocationTargetException.class, () -> method.invoke(app, "hello"), "Error writing to outputstream");

        app.disallowCloseOut();
        Field field = AbstractApp.class.getDeclaredField("isEnd");
        field.setAccessible(true);
        assertEquals(true, field.get(app));

        app.setInputStream(in);
        assertEquals(in, app.getInputStream());


        app.injectCore(jsh.getJshCore());
        Field jshCore = AbstractApp.class.getDeclaredField("jshCore");
        jshCore.setAccessible(true);
        assertEquals(jsh.getJshCore(), jshCore.get(app));

        Field outputStreamLock = AbstractApp.class.getDeclaredField("outputStreamLock");
        outputStreamLock.setAccessible(true);
        app.lockOutputStream();
        assertEquals(true, outputStreamLock.get(app));

        assertDoesNotThrow(() -> method.invoke(app, ""));
    }

    @Test
    public void testTail() {
        App app = FactoryProvider.getAppFactory().create("tail", jsh.getJshCore());
        assertThrows(RuntimeException.class, () -> app.setArgs(new String[] {"-n", "nihao"}));
        assertThrows(RuntimeException.class, () -> app.setArgs(new String[] {"-n", "10", "n", "n"}));

        app.setArgs(new String[] {"-n", "10", "no.txt"});
        assertThrows(RuntimeException.class, app::run);
    }

    @Test
    public void testWc() throws Exception {
        App app = FactoryProvider.getAppFactory().create("wc", jsh.getJshCore());
        Field fromStdIn = Wc.class.getDeclaredField("fromStdIn");
        fromStdIn.setAccessible(true);
        Field countChar = Wc.class.getDeclaredField("countChar");
        countChar.setAccessible(true);

        app.setArgs(new String[]{});
        assertEquals(true, fromStdIn.get(app));

        app.setArgs(new String[]{"-m"});
        assertEquals(true, countChar.get(app));

        Field files = Wc.class.getDeclaredField("files");
        files.setAccessible(true);
        app.setArgs(new String[] {"a.txt", "b.txt"});
        assertEquals("a.txt", ((List<String>) files.get(app)).get(0));
        assertEquals("b.txt", ((List<String>) files.get(app)).get(1));

        jsh.eval("echo hello | wc", out);
        assertEquals("1 1 6", reader.readLine());
    }

    @Test
    public void testWc1() throws Exception {
        jsh.eval("echo hello | wc -m", out);
        assertEquals("6", reader.readLine());
    }


    @Test
    public void testGrep() {
        App app = FactoryProvider.getAppFactory().create("grep", jsh.getJshCore());
        assertThrows(RuntimeException.class, () -> app.setArgs(new String[] {}), "grep: wrong number of arguments");

        app.setArgs(new String[] {"aa", "ttst.txt"});
        assertThrows(RuntimeException.class, app::run);
    }

    @Test
    public void testHead() {
        App app = FactoryProvider.getAppFactory().create("head", jsh.getJshCore());
        assertThrows(RuntimeException.class, () -> app.setArgs(new String[] {"-n", "ss", "sdf"}));
    }

    @Test
    public void testLs() throws Exception {
        Method method = Ls.class.getDeclaredMethod("writeFile", Path.class);
        Path test = Paths.get("/hello/world/test.txt");
        App app = FactoryProvider.getAppFactory().create("ls", jsh.getJshCore());
        app.setOutputStream(out);


        method.setAccessible(true);
        method.invoke(app, test);

        assertEquals("test.txt", reader.readLine());

    }


    @Test
    public void testSed() throws Exception {
        App app = FactoryProvider.getAppFactory().create("sed", jsh.getJshCore());
        assertThrows(RuntimeException.class, () -> app.setArgs(new String[]{}));
        assertThrows(RuntimeException.class, () -> app.setArgs(new String[]{"t"}));
        assertThrows(RuntimeException.class, () -> app.setArgs(new String[]{"t|t"}));
        app.setArgs(new String[]{"a|b|g|g"});

        Field isGlobal = Sed.class.getDeclaredField("isGlobal");
        isGlobal.setAccessible(true);

        assertEquals(true, isGlobal.get(app));

        assertThrows(RuntimeException.class, () -> jsh.eval("sed s/u/a non.txt", out));
    }

    @Test
    public void testGrep1() throws Exception {
        Grep grep = new Grep();
        Method matchFiles = Grep.class.getDeclaredMethod("matchFiles", Pattern.class);
        matchFiles.setAccessible(true);

        Field arguments = Grep.class.getDeclaredField("arguments");
        arguments.setAccessible(true);
        arguments.set(grep, new String[] {"hello", "noex.txt"});

        assertThrows(InvocationTargetException.class, () -> matchFiles.invoke(grep, Pattern.compile("hello")));
    }

    @Test
    public void testHead1() throws Exception {
        jsh.eval("echo nihao | head -n 5 ' '", out);
        assertEquals("nihao", reader.readLine());
    }

    @Test
    public void testSedException() {
        App sed = new Sed();
        assertThrows(RuntimeException.class, sed::run);
    }

    @Test
    public void testSedException1() throws Exception {
        App sed = new Sed();
        Field filePath = Sed.class.getDeclaredField("filePath");
        filePath.setAccessible(true);

        filePath.set(sed, "");
        assertThrows(RuntimeException.class, sed::run);
        filePath.set(sed, " ");
        assertThrows(RuntimeException.class, sed::run);

        assertThrows(RuntimeException.class, () -> sed.setArgs(new String[]{"1", "2", "3"}));
        assertThrows(RuntimeException.class, () -> sed.setArgs(new String[]{"s/a/b/c/d/e"}));
    }

    @Test
    public void testJsh1() throws Exception {
        PrintStream old = System.out;
        PrintStream printStream = new PrintStream(out);
        System.setOut(printStream);
        Jsh.main(new String[] { "nihao", "world!"});
        assertTrue(reader.readLine().endsWith("argument"));
        System.setOut(old);
        ExecutorFactory.reset();
    }

    @Test
    public void testJsh2() {
        String data = "exit\n";
        InputStream inputStream = new ByteArrayInputStream(data.getBytes());

        InputStream old = System.in;
        System.setIn(inputStream);
        assertTimeout(Duration.ofMillis(100), () -> Jsh.main(new String[] {}));

        System.setIn(old);
        ExecutorFactory.reset();
    }

    @Test
    public void testSedGlobal() throws Exception {
        jsh.eval("echo niiosi | sed s/i/a/g ", out);
        assertEquals("naaosa", reader.readLine());
    }

    @Test
    public void testUnsafe() {
        App app = FactoryProvider.getAppFactory().create("_sed", jsh.getJshCore());

        assertDoesNotThrow(() -> app.setArgs(new String[] {}));
        assertDoesNotThrow(app::run);
    }
    @Test
    public void testJshMain() throws Exception {
        String[] args = new String[] {"-c", "echo hello", "test"};
        PrintStream printStream = new PrintStream(out);
        PrintStream old = System.out;

        System.setOut(printStream);
        Jsh.main(args);
        assertEquals("jsh: wrong number of arguments", reader.readLine());

        System.setOut(old);
        printStream.close();
    }

    @Test
    public void testAppNotFoundException() {
        AppNotFoundException exception = new AppNotFoundException("test");
        assertEquals("test", exception.getMessage());
    }

    @Test
    public void testFactoryProvider() throws Exception {
        Field field = FactoryProvider.class.getDeclaredField("appFactory");
        field.setAccessible(true);

        IAbstractAppFactory factory1 = FactoryProvider.getAppFactory();
        field.set(null, null);

        assertNotEquals(factory1, FactoryProvider.getAppFactory());
    }

    @Test
    public void testAppFactoryException() {
        IAbstractAppFactory appFactory = FactoryProvider.getAppFactory();

        assertThrows(AppNotFoundException.class, () -> appFactory.create("test"));
    }

    @Test
    public void testAppFactory() throws Exception {
        IAbstractAppFactory appFactory = FactoryProvider.getAppFactory();
        assertEquals(Unsafe.class.getCanonicalName(), appFactory.create("_cd").getClass().getCanonicalName());
        assertEquals(Unsafe.class.getCanonicalName(), appFactory.create("_cat").getClass().getCanonicalName());
        assertEquals(Unsafe.class.getCanonicalName(), appFactory.create("_echo").getClass().getCanonicalName());
        assertEquals(Unsafe.class.getCanonicalName(), appFactory.create("_pwd").getClass().getCanonicalName());
        assertEquals(Unsafe.class.getCanonicalName(), appFactory.create("_tail").getClass().getCanonicalName());
        assertEquals(Unsafe.class.getCanonicalName(), appFactory.create("_head").getClass().getCanonicalName());
        assertEquals(Unsafe.class.getCanonicalName(), appFactory.create("_grep").getClass().getCanonicalName());
        assertEquals(Unsafe.class.getCanonicalName(), appFactory.create("_sed").getClass().getCanonicalName());
        assertEquals(Unsafe.class.getCanonicalName(), appFactory.create("_find").getClass().getCanonicalName());
        assertEquals(Unsafe.class.getCanonicalName(), appFactory.create("_wc").getClass().getCanonicalName());

        App app = appFactory.create("cd", jsh.getJshCore());
        Field field = AbstractApp.class.getDeclaredField("jshCore");
        field.setAccessible(true);

        assertEquals(jsh.getJshCore(), field.get(app));
    }


    @Test
    public void testMakePipeline() {
        Command command = new Command(" ");
        List<List<Command>> list = new ArrayList<>();
        list.add(new ArrayList<>());
        list.get(0).add(command);
        assertThrows(RuntimeException.class, () -> jsh.eval(list, out), "Command Type Missing");

        command.setCommandType(null);
        assertThrows(RuntimeException.class, () -> jsh.eval(list, out), "Command Type Missing");
    }

    @Test
    public void testInputRedirect() {
        assertThrows(RuntimeException.class, () -> jsh.eval("cat < dir1/no.txt", out), "Input Redirect File Not Found");
    }

    @Test
    public void testInputOutputRedirect() {
        assertThrows(RuntimeException.class, () -> jsh.eval("cat < dir1/no.txt > dir1/out.txt", out), "Input Redirect File Not Found");
    }

    @Test
    public void testInputOutputRedirect1() throws Exception {
        File biml = IntelligentPath.getPath("b.iml", dir1.toPath()).toFile();
        boolean pre = biml.exists();
        jsh.eval("cd dir1; cat < a.iml > b.iml", out);
        assertNotEquals(pre, biml.exists());
        biml.delete();
    }

    @Test
    public void testEmptyCommand() throws Exception {
        jsh.eval("", out);
        assertEquals('\n', reader.read());
    }

    @Test
    public void testJshEval() throws Exception {
        jsh.eval(" ", out);
        assertEquals('\n', reader.read());
    }

    @Test
    public void testJshEvalException() {
        List<List<Command>> l = new ArrayList<>();
        List<Command> list = new ArrayList<>();
        list.add(new Command(" "));
        l.add(list);

        assertThrows(RuntimeException.class, () -> jsh.eval(l, out));
    }

    @Test
    public void testFind() throws Exception {
        jsh.eval("find -name \"dir1/*.iml\"", out);
        assertEquals("./dir1/a.iml", reader.readLine());
    }

    @Test
    public void testJshFind1() throws Exception {
        jsh.eval("cd dir1; find -name \"*xt\"", out);
        assertEquals("./b.txt", reader.readLine());
    }

    @Test
    public void testJshFind2() throws Exception {
        expectedException.expect(RuntimeException.class);
        jsh.eval("find", out);
    }

    @Test
    public void testJshFind3() throws Exception {
        expectedException.expect(RuntimeException.class);
        jsh.eval("find dir3 -name *.iml", out);
    }

    @Test
    public void testJshFind4() throws Exception {
        jsh.eval("find dir1 -name '*.txt'", out);
        assertEquals("dir1/b.txt", reader.readLine());
    }

    @Test
    public void testJshCat() throws Exception {
        jsh.eval("echo hello | cat", out);
        assertEquals("hello", reader.readLine());
    }

    @Test
    public void testJshCat1() throws Exception {
        expectedException.expect(RuntimeException.class);
        jsh.eval("cat amil", out);
    }

    @Test
    public void testJshCd() throws Exception {
        jsh.eval("cd dir1", out);
        assertEquals(dir1.toPath().toAbsolutePath().toString(), jsh.getJshCore().getCurrentDirectory().toString());
    }

    @Test
    public void testJshCd1() throws Exception {
        expectedException.expect(RuntimeException.class);
        jsh.eval("cd aiml", out);
    }

    @Test
    public void testJshCd2() throws Exception {
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Arguments do not match with the program");
        jsh.eval("cd", out);
    }

    @Test
    public void testJshEcho() throws Exception {
        jsh.eval("echo hello", out);
        assertEquals("hello", reader.readLine());
    }

    @Test
    public void testJshEcho1() throws Exception {
        jsh.eval("`echo echo` hello", out);
        assertEquals("hello", reader.readLine());
    }

    @Test
    public void testJshEcho2() throws Exception {
        jsh.eval("echo \"hello `echo world!`\"", out);
        assertEquals("hello world!", reader.readLine());
    }



    @Test
    public void testJshGrep() throws Exception {
        jsh.eval("echo \"nihao\" | grep iha", out);
        assertEquals("nihao", reader.readLine());
    }

    @Test
    public void testJshGrep1() throws Exception {
        jsh.eval("grep llo dir1/a.iml", out);
        assertEquals("hello", reader.readLine());
    }

    @Test
    public void testJshGrep2() throws Exception {
        expectedException.expect(RuntimeException.class);
        jsh.eval("grep llo dir1", out);
    }

    @Test
    public void testJshHead() throws Exception{
        jsh.eval("cat \"dir1/a.iml\" | head", out);
        assertEquals("hello", reader.readLine());
        assertEquals("world!", reader.readLine());
    }

    @Test
    public void testJshHead1() throws Exception{
        jsh.eval("head dir1/a.iml", out);
        assertEquals("hello", reader.readLine());
        assertEquals("world!", reader.readLine());
    }

    @Test
    public void testJshHead2() throws Exception{
        jsh.eval("head -n 15 dir1/a.iml", out);
        assertEquals("hello", reader.readLine());
        assertEquals("world!", reader.readLine());
    }

    @Test
    public void testJshHead3() throws Exception{
        jsh.eval("cat \"`echo dir1/a.iml`\" | head -n 15", out);
        assertEquals("hello", reader.readLine());
        assertEquals("world!", reader.readLine());
    }

    @Test
    public void testJshHead4() throws Exception{
        expectedException.expect(RuntimeException.class);
        jsh.eval("head -n 15 test.txt", out);
    }

    @Test
    public void testJshHead5() throws Exception{
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Bad Arguments");
        jsh.eval("head -k 15 dir1/a.iml", out);
    }

    @Test
    public void testJshHead6() throws Exception{
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("head: wrong arguments");
        jsh.eval("head -k 15 dir1/a.iml dir1/b.txt", out);
    }

    @Test
    public void testJshLs() throws Exception{
        jsh.eval("cd dir2; ls", out);
        assertNull( reader.readLine());
    }

    @Test
    public void testJshLs2() throws Exception{
        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("Arguments do not match with the program");
        jsh.eval("ls arg1 arg2", out);
    }

    @Test
    public void testJshLs3() throws Exception{
        expectedException.expect(RuntimeException.class);
//        expectedException.expectMessage("Only directory path can be used as argument");
        jsh.eval("ls dir1/a.iml", out);
    }

    @Test
    public void testJshLs4() throws Exception{
        jsh.eval("ls dir1", out);
        String[] filesName = { "a.iml", "b.txt", "dir3" };
        assertTrue(Arrays.asList(filesName).containsAll(Arrays.asList(reader.readLine().split("\\s+"))));
    }

    @Test
    public void testJshPwd() throws Exception {
        jsh.eval("pwd", out);
        assertEquals(jsh.getJshCore().getCurrentDirectory().toAbsolutePath().toString(), reader.readLine());
    }

    @Test
    public void testJshSed() throws Exception {
        jsh.eval("sed s/hello/nihao dir1/a.iml", out);
        assertEquals("nihao", reader.readLine());
        assertEquals("world!", reader.readLine());
    }

    @Test
    public void testJshSed1() throws Exception {
        jsh.eval("cat dir1/a.iml | sed s/llo/bbo", out);
        assertEquals("hebbo", reader.readLine());
    }

    @Test
    public void testJshTail() throws Exception{
        jsh.eval("tail dir1/a.iml", out);
        assertEquals("hello", reader.readLine());
        assertEquals("world!", reader.readLine());
    }

    @Test
    public void testJshTail1() throws Exception{
        jsh.eval("tail -n 1 `find -name dir1/*.iml`", out);
        assertEquals("world!", reader.readLine());
    }

    @Test
    public void testJshTail2() throws Exception{
        jsh.eval("echo foo | tail", out);
        assertEquals("foo", reader.readLine());
    }

    @Test
    public void testJshWc() throws Exception{
        jsh.eval("wc -l dir1/a.iml", out);
        assertEquals("2", reader.readLine());
    }

    @Test
    public void testJshWc1() throws Exception{
        jsh.eval("wc -w dir1/a.iml", out);
        assertEquals("2", reader.readLine());
    }

    @Test
    public void testJshWc3() throws Exception{
        jsh.eval("cat dir1/a.iml | wc -l", out);
        assertEquals("2", reader.readLine());
    }

    @Test
    public void testJshWc4() throws Exception{
        jsh.eval("cd dir1; wc -l b.txt a.iml", out);
        assertEquals("2",reader.readLine());
    }

    @Test
    public void testJshWc5() throws Exception{
        jsh.eval("cd dir1; wc -w c.txt", out);
        assertEquals("0",reader.readLine());
    }

    @Test
    public void testJshWc6() throws Exception{
        jsh.eval("wc -w dir1", out);
        assertEquals("0",reader.readLine());
    }

    @Test
    public void testJshRedirection() throws Exception{
        jsh.eval("cat < dir1/a.iml", out);
        assertEquals("hello",reader.readLine());
    }

    @Test
    public void testJshRedirectionInfront() throws Exception{
        jsh.eval("<dir1/a.iml cat", out);
        assertEquals("hello",reader.readLine());
    }

    @Test
    public void testJshGlobbing() throws Exception{
        jsh.eval("cd dir1; echo *", out);
        String[] filesName = { "a.iml", "b.txt", "dir3" };
        assertTrue(Arrays.asList(filesName).containsAll(Arrays.asList(reader.readLine().split("\\s+"))));
    }

    @Test
    public void testJshGlobbingDir() throws Exception{
        jsh.eval("echo dir1/*.txt", out);
        assertEquals("dir1/b.txt", reader.readLine());
    }

    @Test
    public void testJshSemicolon() throws Exception{
        jsh.eval("echo aaa; echo bbb", out);
        assertEquals("aaa", reader.readLine());
        assertEquals("bbb", reader.readLine());
        assertNull(reader.readLine());
    }

    @Test
    public void testJshSemicolon1() throws Exception{
        jsh.eval("echo AAA; echo BBB; echo CCC", out);
        assertEquals("AAA", reader.readLine());
        assertEquals("BBB", reader.readLine());
        assertEquals("CCC", reader.readLine());
        assertNull(reader.readLine());
    }

    @Test
    public void testJshPipe() throws Exception{
        jsh.eval("echo AAA | sed s/A/B/", out);
        assertEquals("BAA", reader.readLine());
    }

    @Test
    public void testJshPipe1() throws Exception{
        jsh.eval("echo AAA | sed s/A/C/ | sed s/A/B/", out);
        assertEquals("CBA", reader.readLine());
    }

    @Test
    public void testJshSubstitution() throws Exception{
        jsh.eval("echo `echo foo`", out);
        assertEquals("foo", reader.readLine());
    }

    @Test
    public void testJshSubstitution1() throws Exception{
        jsh.eval("echo a`echo a`a", out);
        assertEquals("aaa", reader.readLine());
    }

    @Test
    public void testJshSubstitution2() throws Exception{
        jsh.eval("echo `echo foo  bar`", out);
        assertEquals("foo bar", reader.readLine());
    }

    @Test
    public void testJshSubstitutionSemicolon() throws Exception{
        jsh.eval("echo `echo foo; echo bar`", out);
        assertEquals("foo bar", reader.readLine());
    }

    @Test
    public void testJshSubstitutionKeywords() throws Exception{
        jsh.eval("echo `cat dir1/a.iml`", out);
        assertEquals("hello world!", reader.readLine());
    }

    @Test
    public void testJshSubstitutionApp() throws Exception{
        jsh.eval("`echo echo` foo", out);
        assertEquals("foo", reader.readLine());
    }

    @Test
    public void testJshSingleQuotes() throws Exception{
        jsh.eval("echo 'a  b'", out);
        assertEquals("a  b", reader.readLine());
    }

    @Test
    public void testJshQuoteKeywords() throws Exception{
        jsh.eval("echo ';'", out);
        assertEquals(";", reader.readLine());
    }

    @Test
    public void testJshDoubleQuotes() throws Exception{
        jsh.eval("echo \"a  b\"", out);
        assertEquals("a  b", reader.readLine());
    }

    @Test
    public void testJshSubstitutionDoubleQuotes() throws Exception{
        jsh.eval("echo \"`echo foo`\"", out);
        assertEquals("foo", reader.readLine());
    }

    @Test
    public void testJshNestedDoubleQuotes() throws Exception{
        jsh.eval("echo \"a `echo \"b\"`\"", out);
        assertEquals("a b", reader.readLine());
    }

    @Test
    public void testJshDisplayedDoubleQuotes1() throws Exception{
        jsh.eval("echo '\"\"'", out);
        assertEquals("\"\"", reader.readLine());
    }

    @Test
    public void testJshSplitting() throws Exception{
        jsh.eval("echo a\"b\"c", out);
        assertEquals("abc", reader.readLine());
    }

    @Test
    public void testUnsafeLs() throws Exception {
        jsh.eval("_ls sadfok; echo hello", out);
        assertEquals("hello", reader.readLine());
    }

    @Test
    public void testExecutorFactory() {
        ExecutorService executorService1 = ExecutorFactory.getExecutorService();
        ExecutorService executorService2 = ExecutorFactory.getExecutorService();

        assertEquals(executorService1, executorService2);
    }

    @Test
    public void testJsh() throws Exception {
        System.setOut(new PrintStream(out));
        Jsh.main(new String[] { "-c", "echo hello"});
        assertEquals("hello", reader.readLine());
        System.setOut(System.out);
        ExecutorFactory.reset();
    }

    @Test
    public void testPair() {
        Pair<String, String> pair = new Pair<>("hello", "world!");
        assertEquals("hello", pair.getKey());
        assertEquals("world!", pair.getValue());
        pair.setValue("olleh");
        assertEquals("olleh", pair.getValue());
    }

    @Test
    public void testCommandException() {
        IParser parser = new Parser();
        assertThrows(IllegalArgumentException.class, () -> parser.parse("echo wocao <"));
    }

    @Test
    public void testCommandException1() {
        IParser parser = new Parser();
        assertThrows(IllegalArgumentException.class, () -> parser.parse("echo wocao >"));
    }

    @After
    public void after() throws Exception {

        reader.close();
        in.close();
        out.close();
    }

    @AfterClass
    public static void afterClass() {
        btxt.delete();
        aiml.delete();
        dotc.delete();


        dir3.delete();

        dir1.delete();
        dir2.delete();
    }
}
