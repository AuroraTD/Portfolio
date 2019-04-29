# CSC582_Final_Project #

## Team TAVA ##

This README describes how to use the children's story generator web application.
* For instructions on how to launch the application, please see ./bedtime-story/README.md

### Header ###

The header appears at the top of your browser window, no matter which page you are on.  You can click the "Home" button to go back to the Home page at any time.

### Home Page ###

The home page is the first page to appear.  You can return to this page at any time by clicking the "Home" button on the header at the top of your browser window.

This page welcomes you to the application, and has just one option - "Start" - which loads the Age page.

![image](https://media.github.ncsu.edu/user/10688/files/47d5d400-6522-11e9-9076-b5ff94b7a52d)

### Age Page ###

This page is loaded after you click "Start" on the Home page.

This page lets you or your child select your child's age.  Your child's age is taken into account in several ways.
* Older children are asked more questions on the Questionnaire page.
* Older children have more options to choose from, for each question on the Questionnaire page.
* Older children's stories will include more challenging vocabulary (like "gratifying" versus "nice").
* _Possibly: older children's stories will have more complex sentence structure._
* _Possibly: older children's stories will be longer._

Once you have selected your child's age, you may click the "Next" button which loads the Menu page.

![image](https://media.github.ncsu.edu/user/10688/files/676cfc80-6522-11e9-8815-565244377aee)

### Menu Page ###

This page is loaded after you click "Next" on the Age page.

This page lets you or your child choose whether or not they wish to answer questions that will help to build their story.

![image](https://media.github.ncsu.edu/user/10688/files/85d2f800-6522-11e9-850f-3e8aceb094ea)


If you click "Yes", then the Questionnaire page will load.
* This page will ask your child some questions, and their answers will influence the story.

If you click "No", then the Storybook page will load.
* This will still generate a new story each time.
* If you choose this option, our application makes random choices for you.

### Questionnaire Page ###

![image](https://media.github.ncsu.edu/user/10688/files/b1ee7900-6522-11e9-9a66-d8960157e964)

This page is loaded if you click "Yes" on the Menu page.

This page shows your child several questions (more questions are shown to older children), and several options to choose from for each question (more options are shown to older children).  To choose an option, simply click on it.  If you change your mind, simply click on a different option.

All of the questions are optional - if you don't want to choose, just don't click any of the options.  In this case, our application chooses a random option for you.

Some of the questions your child might be asked:
* Which girl's name do you like the best?
* Which boy's name do you like the best?
* Which color do you like the best?
* Which theme do you like the best?
* Which animal do you like the best?
* Which location do you like the best?

Some of the options include images.  These images are hand-selected, so they will not include anything that isn't "family-friendly".

The options your child chooses influence the story they are told.

Once your child has answered all of the questions that interest them, you may click "Tell me a story!" which loads the Storybook page.

### Storybook Page ###

![image](https://media.github.ncsu.edu/user/10688/files/f843d800-6522-11e9-99a5-72bdcd6c51a7)

This page is loaded if you click "No" on the Menu page, or if you click "Tell me a story!" on the Questionnaire page.

This page displays your child's story.  The story is influenced by your child's age and by their choices, but also has an element of randomization.
* Since there is a limit to how many questions your child is asked on the Questionnaire page, there is at least one question for which the application made a random choice.
* The application also randomizes the story flow and the vocabulary used in the story, every time a story is generated.

When your child is finished reading the story, they are asked "Do you want to make another story?".
* If you click "Yes", then the Age page is loaded.
* If you pick "No", then the Home page is loaded.
