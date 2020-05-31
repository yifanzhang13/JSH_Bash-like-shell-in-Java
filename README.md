# COMP0010-JSH

This software runs on a custom low power consumption operating system. The main user interface of this system is a homegrown shell called JSH. Since its inception, the company relies on this shell, and many scripts and commands have been already written by the users and developers of the system. However, the functionality and stability of the shell do not longer meet the requirements of the growing business. At the same time, legacy code makes it too expensive to switch to a different shell implementation. Your manager asks you and two other novice developers to extend the capabilities of JSH, while preserving the original features and fixing existing bugs. After examining the source code of JSH, you and your colleagues realize that it was developed without following software engineering practices, and as a result, it is extremely difficult to extend the implementation without introducing new bugs which will lead to problems in production. Thus, the first step is to refactor the legacy implementation to make it extendable, and add a regression test-suite to avoid functionality-breaking changes. You receive the source code of the shell, and an incomplete specification of its functionality, as well as a description of the desired extension. In order to complete this task, you will need to apply the principles and techniques that you learned from COMP0010 Software Engineering course.