"ScienceLab" by Aurora

When play begins:
	say "It's 6:00 PM.  You arrive for your regular evening shift at the research institute and flip on the lights.  Good grief.  It's a mess!  Stuff strewn everywhere... Is that... blood?  No, it can't be.  Where is the key to the supply closet where you keep your cleaning materials?  It's normally hanging right by the front door!  As the janitor of this fine institution, it is your job to clean up the mess.  What on earth were these scientists up to?  Can you clean up by the time your shift ends at 2:00 AM?".

[Help]
[source: http://inform7.com/learn/man/RB_6_2.html]

Understand "help" as helping.
Helping is an action applying to nothing.
Carry out helping:
	say "You can 'go' north, south, east, or west (if there is something in that direction).  You can 'look' or 'smell' or 'listen' to get your bearings.  If there is a rock nearby, you can 'look at' the rock, 'smell' the rock, 'listen to' the rock, 'taste' the rock, or 'feel' the rock.  You an also 'take' or 'pick up' the rock, 'put' the rock 'on' something else, or if you have the rock you can 'drop' the rock.  There are other commands too.  No harm in trying to do something by typing it out!  If you can't remember what you're carrying, type 'inventory'.  If you wish to end the game by leaving the building, type 'exit'.".
After printing the banner text, say "[paragraph break]****** TYPE 'help' TO LEARN SOME COMMANDS YOU CAN USE ******[paragraph break]".

[Directions]
[source: https://stackoverflow.com/questions/42182740/how-do-i-show-the-available-rooms-in-inform7]

Definition: a direction (called thataway) is viable if the room 
  thataway from the location is a room.

After looking:
	say "You can go [list of viable directions] from here."

[Seen / Unseen]

A thing can be seen or unseen.
After printing the name of something (called the target): 
	now the target is seen.

[Clean / Dirty]
[source: http://docs.textadventures.co.uk/ifanswers/ifanswers.com/947/inform-multiple-instances-thing-where-some-change-properties.html]

Cleanliness is a kind of value. 
The cleanlinesses are clean and dirty.
A thing has a cleanliness. 
The cleanliness of a thing is usually dirty.
The cleanliness of yourself is clean.
Before printing the name of a thing, say "[cleanliness] ".
Before printing the plural name of a thing, say "[cleanliness] ".
Understand the cleanliness property as describing a thing.

A thing has a text called dirtyText.

Cleaning is an action applying to one thing.
Understand "clean [thing]" as cleaning.

Check cleaning:
	if the noun is clean:
		say "Why would you need to clean [the noun]?" instead;
	otherwise if the player carries the bottle of pine sol and the player carries the rag:
		say "That oughta do it!";
		now the noun is clean;
		now the odor of the noun is "fresh mountain pine scent";
		increase the time of day by insanityLevel minutes;
		if the noun is not the rag:
			now the rag is dirty;
	otherwise if the player does not carry the rag:
		say "You rub [the noun] with your hands, but it doesn't do any good.  Perhaps if you had cleaning supplies (rag & pine sol)?" instead;
	otherwise if the player carries the rag:
		say "You rub [the noun] with the dry rag, but it doesn't do much good.  Perhaps if you had pine sol?" instead.

After examining:
	if the noun is dirty:
		say "[the dirtyText of the noun][paragraph break]".
		
To decide if the cleanup is good enough:
	let aDirtyThings be the list of things that are dirty;
	let nNumDirtyThings be the number of entries in aDirtyThings;
	decide on whether or not nNumDirtyThings < 4.
	
[Touch]

A thing has some text called feeling.
The feeling of a thing is usually "nothing".

Instead of touching something: 
	if the feeling of the noun is "nothing":
		say "[the noun] feels about like you'd expect.";
	otherwise:
		say "[the noun] feels [the feeling of the noun].".

[Taste]

A thing has some text called taste.
The taste of a thing is usually "nothing".

Instead of tasting something: 
	if the taste of the noun is "nothing":
		say "[the noun] tastes about like you'd expect.";
	otherwise:
		say "[the noun] tastes [the taste of the noun].".

[Smell]
[source: http://ifwiki.org/index.php/Smell_restrictions_(Inform_7_example)]

A thing has some text called odor.
The odor of a thing is usually "nothing".

The report smelling rule is not listed in the report smelling rules.

Carry out smelling something: 
	say "From [the noun] you smell [the odor of the noun]."
	
After smelling something:
	increase insanityLevel by 3;
	say "You smell... something... very faintly... something you can't quite identify.  You take another big whiff.  You feel a bit light-headed.".
	
Instead of smelling a room: 
	if a smelly thing can be touched by the player, say "You smell [the list of smelly things which can be touched by the player]."; 
	increase insanityLevel by 3;
	say "You smell... something... very faintly... something you can't quite identify.  You take another big whiff.  You feel a bit light-headed.".
	
Definition: a thing is smelly if the odor of it is not "nothing" and the odor of it is not "fresh mountain pine scent".

Before printing the name of something smelly while smelling a room: 
	say "[odor] from the ".

[After smelling:
   say "[if the odor of the noun is not empty][the odor of the noun][paragraph break][otherwise]You smell nothing unexpected.[end if]";]

[Sound]
[source: http://inform7.com/learn/man/RB_3_8.html#]

A thing has some text called sound. 
The sound of a thing is usually "silence".

The report listening rule is not listed in the report listening to rules.

Carry out listening to something: 
	say "From [the noun] you hear [the sound of the noun]."
	
Instead of listening to a room: 
	if an audible thing can be touched by the player, say "You hear [the list of audible things which can be touched by the player]."; 
	otherwise say "Dead silence."
	
Definition: a thing is audible if the sound of it is not "silence".

Before printing the name of something audible while listening to a room: 
	say "[sound] from the "
	
[Passage of Time / Sanity / End of Game]
[source: http://inform7.com/learn/man/WI_9_9.html]

insanityLevel is a number that varies.

When play begins: 
	now the time of day is 6:00 PM;
	let insanityLevel be 1.
	
To have insane thought:
	say "***** [one of]You're starting to feel a bit funny.[or]You feel a bit overwhelmed.[or]Your teeth are tingling.[or]You're finding yourself distracted by the smallest things.  Why is everything taking so long?[or]You think of the Pink Floyd song... your hands feel just like two balloons.[or]You hear a lovely song... where is that coming from?[or]You check to make sure you still have 10 fingers... fingers are weird... it takes a while because they keep moving around.[or] You've made up a new song!  You're sure it'll be a chart topper... if the general public is capable of appreciating its true genius.[or]Your shirt is really starting to annoy you.  Why do we have to wear shirts anyway?  It's only separating you from the TRUTH.  It's a conspiracy, you're sure of it.[or]You count all the tiles in the floor, and then count them again.  Some are missing.  Someone must have been stealing tiles while you've been counting![or]You're carrying [a list of things carried by the player] and it all feels like such a burden.[or]You suddenly have the urge to ball up your fists and punch your leg.  You restrain yourself.[or]How dare these scientists have created such a mess for you to clean up.  They're gonna regret this.[or]This place is just so beautiful sometimes that you want to cry.[or]You hear someone whisper your name.[or]You see a fat little bluebird fly past.[or]Your scalp feels the gentle massage of 1,000 beautiful fairies.[or]You can feel ants crawling up your legs.  You kinda like it![or]You realize that time is a meaningless construct.[or]Time seems to stretch like silly putty and then suddenly snap back in your face.  It leave you feeling off-balance.[or]You wonder how long you have been here.  It feels like mere seconds.[or]You wonder how long you have been here.  It feels like days.[purely at random]".

Every turn: 
	increase insanityLevel by 1.
	
Every turn:
	if a random chance of 1 in 10 succeeds:
		say "[one of]There is still more cleaning to do.[or]This place is a wreck.[or]No rest for the weary janitor.[or]Cleaning is hard work.[or]Not spotless yet.[or]Still some grubby stuff around.[or]The institute still doesn't look as nice as it could.[or]The place could still use a bit of elbow grease.[purely at random]".
		
Every turn:
	if the cleanup is good enough:
		end the story saying "It's [time of day].  The place is pretty clean, or close enough for government work anyway!  You lock up, go home, and sleep soundly.  GOOD JOB!".
		
Every turn:
	if the time of day is after 2:00 AM: [shift should end well before Inform7 day rollover of 4:00 AM to make sure we catch the deadline]
		end the story saying "It's [time of day].  Your shift is over and the institute still isn't clean ([list of things that are dirty]).  You lock up, go home, and fret about your boss chewing you out tomorrow.".
		
Every turn:
	if insanityLevel is:
		-- 10: have insane thought;
		-- 20: have insane thought;
		-- 25: have insane thought;
		-- 30: have insane thought;
		-- 35: have insane thought;
		-- 40: have insane thought;
		-- 45: have insane thought;
		-- 50: have insane thought;
		-- 55: have insane thought;
		-- 60: have insane thought;
		-- 65: have insane thought;
		-- 70: have insane thought;
		-- 75: have insane thought;
		-- 80: have insane thought;
		-- 85: have insane thought;
		-- 90: have insane thought;
		-- 95: have insane thought;
		-- 96: have insane thought;
		-- 97: have insane thought;
		-- 98: have insane thought;
		-- 99: have insane thought;
		-- 100: end the story saying "You go stark raving mad, tearing at your clothes and hair.  You wreck everything in sight.  The sound wakes the neighbors and the police are called.  They cart you away, never to see your family, or your sanity, again.".

Before exiting:
	end the story saying "It's [time of day].  You throw up your hands.  You'll never get this place clean, and anyway you're really starting to get creeped out.  You lock up, go home, and call your brother-in-law to see if he can get you a job.".
	
[Hidden Key]

There is a closet key.
The description of the closet key is "Heavy brass key.".
The dirtyText of the closet key is "Looks pretty dingy.".
The closet key unlocks the east door.
The feeling of the closet key is "heavy".

After taking something:
	if a random chance of 1 in 3 succeeds:
		if the player does not have the closet key:
			now the player has the closet key;
			say "You have found the supply closet key!  Thank goodness.  You grab it.";
	Continue the action;

[Hallway]

Hallway is a room.
"A large entry hallway with lots of natural light and a high ceiling.  The ceiling is a mosaic pattern of red, blue, and green tiles, producing a slight echo.   The walls are beige."

The hallway floor is here.
The description of the hallway floor is "Slightly scuffed linoleum floor.".
The dirtyText of the hallway floor is "There are muddy footprints all over the floor, and what looks like a smear of blood.".
The odor of the hallway floor is "muck and the metallic tang of blood".
The taste of the hallway floor is "decidedly floor-like".
The hallway floor is fixed in place.
The hallway floor is a supporter.

The lab notebook is on the hallway floor.
The description of the lab notebook is "Scientific lab notebook, unintelligible to a layperson."
The dirtyText of the lab notebook is "Covered in greasy handprints.".
The feeling of the lab notebook is "like rough canvas".

[Classroom]

Classroom is a room.
"A small classroom, mostly empty, used when the institute puts on lectures for the public.  Also used for training sessions for employees and staff.  Drab green."

The classroom floor is here.
The description of the classroom floor is "Slightly scuffed linoleum floor."
The dirtyText of the classroom floor is "There are muddy footprints and scribbled chalk marks on the floor.".
The odor of the classroom floor is "dirty feet and chalk dust".
The classroom floor is fixed in place.
The classroom floor is a supporter.

The chemistry textbook is on the classroom floor.
The description of the chemistry textbook is "Chemistry textbook.".
The dirtyText of the chemistry texbook is "Covered in handprints.".
The feeling of the chemistry textbook is "like glossy cardboard".

The journal article is on the classroom floor.
The description of the journal article is "Long complicated neuroanatomy journal article.  The reference section alone is 4 pages long.".
The feeling of the journal article is "heavy".
The dirtyText of the journal article is "Covered in handprints.".
The taste of the journal article is "like paper, obviously".

The trash can is on the classroom floor.
The description of the trash can is "Small plastic rectangular trashcan.".
The dirtyText of the trash can is "Garbage juice sloshes around in the bottom when you kick it.".
The odor of the trash can is "rotten food and pungent desperation of grad student vomit".
The taste of the trash can is "truly awful".
The feeling of the trash can is "brittle".
The trash can is a container.

The bookshelf is here.
The description of the bookshelf is "An old brown bookshelf, worn at the edges by generations of nerds".
The dirtyText of the bookshelf is "Very dusty.".
The odor of the bookshelf is "lovely library book smell, tainted by dust and something organic and unsettling".
The feeling of the bookshelf is "like good sturdy solid wood".
The bookshelf is fixed in place.
The bookshelf is a supporter.

[Supply Closet]

Supply Closet is a room.
"A small supply closet.  Your home away from home.  Overly warm, a bit stuffy."

The supply closet floor is here.
The description of the supply closet floor is "Linoleum tile flooring. Clean, except for maybe a little dust in the corners."
The supply closet floor is fixed in place.
The supply closet floor is a supporter.
The cleanliness of the supply closet floor is clean.

The workbench is here.
The description of the workbench is "Beat-up old wooden work bench."
The feeling of the workbench is "like good sturdy solid wood".
The workbench is a supporter.
The workbench is fixed in place.
The cleanliness of the workbench is clean.

The rag is on the workbench.
The description of the rag is "Ah, trusty old rag.".
The feeling of the rag is "old and threadbare".
The cleanliness of the rag is clean.

The hammer is on the workbench.
The description of the hammer is "Your typical hammer with a wooden handle and metal head.  The head of the hammer is bashy on one end and pokey on the other.".
The feeling of the hammer is "heavy".
The taste of the hammer is "metallic, with hints of wood".
The cleanliness of the hammer is clean.

The measuring tape is on the workbench.
The description of the measuring tape is "Metal measuring tape with belt clip.".
The feeling of the measuring tape is "cold".
The cleanliness of the measuring tape is clean.

The bottle of pine sol is on the workbench.
The description of the bottle of pine sol is "Your wet amber sidekick.".
The odor of the bottle of pine sol is "cool fresh scent of a manly mountain retreat".
The feeling of the bottle of pine sol is "smooth".
The taste of the bottle of pine sol is "mouth-puckeringly astringent".
The cleanliness of the bottle of pine sol is clean.

[Laboratory]

Laboratory is a room.
"A cold and clinical place, this room is all white tile.  This appears to be the site of an experiment gone wrong."

The laboratory floor is here.
The description of the laboratory floor is "Linoleum tile flooring.  Typical for this kind of research laboratory.".
The dirtyText of the laboratory floor is "Filthy, dusty, greasy.".
The odor of the laboratory floor is "heavy chemical scent".
The laboratory floor is fixed in place.
The laboratory floor is a supporter.

The clock is here.
The description of the clock is "Small plastic and glass clock, reading [time of day].".
The feeling of the clock is "like cheap plastic".
The dirtyText of the clock is "A bit dusty.".
The sound of the clock is "loud ticking".

The whiteboard is here.
The description of the whiteboard is "A huge whiteboard on the west wall.".
The dirtyText of the whiteboard is "Covered in obscene drawings and crazy scribbles.  Certainly the scientists didn't do this?".
The odor of the whiteboard is "grape, apple, and bubble gum marker scent".
The feeling of the whiteboard is "smooth and glossy".
The taste of the whiteboard is "a total blank - tastes like literal nothingness".
The whiteboard is fixed in place.

The table is here.
The description of the table is "Long stainless steel table.".
The dirtyText of the table is "Splattered with green liquid.".
The feeling of the table is "cold".
The table is a supporter.
The table is fixed in place.

The beaker is on the table.
The beaker is a container.
The description of the beaker is "Skinny glass graduated beaker.".
The dirtyText of the beaker is "Splattered with green liquid.". 

The liquid is in the beaker.
The description of the liquid is "Deep emerald green liquid with slowly rising bubbles.".
The odor of the liquid is "astrigent, almost alcoholic scent".
The sound of the liquid is "fizzing".
The cleanliness of the liquid is clean.
The taste of the liquid is "intriguing".
Instead of drinking the liquid: 
	now the liquid is nowhere; 
	say "Bottoms up!  Ohh.... uhhh.... kind of burns.... and not just your throat.  It feels like your mind itself is on fire.";
	increase insanityLevel by 10.
	
[Secret Lab]

Secret Lab is a room.
"Dimly light, this room has an air of mystery.  It is small, cold, and carries with it the memories of furtive meetings and rushed experiments.  Not a place to get comfortable."

The apparatus is here.
The description of the apparatus is "A huge scientific apparatus of some sort.  Glass tubes full of multicolored liquids, electronics, blinking lights, the whole works.  You don't know what it is exactly but it looks expensive and intimidating.  Labeled 'Gietat Luncenifier'".
The sound of the apparatus is "intermittent bleeps and buzzes".
The feeling of the apparatus is "intimidating".
The cleanliness of the apparatus is clean.

The desk is here.
The description of the desk is "Actually just a piece of plywood laid across some plastic crates.  Works well enough.".
The feeling of the desk is "like a cheap do-it-yourself job".
The desk is a supporter.
The desk is fixed in place.
The cleanliness of the desk is clean.

The pen is on the desk.
The description of the pen is "A ballpoint pen. It looks professional."
The feeling of the pen is "expensive".
The taste of the pen is "good, in a bland kind of way - a good pen to have in your mouth while thinking through a problem".
The cleanliness of the pen is clean.

The note is on the desk.
The description of the note is "Milk, eggs, mouthwash, toilet paper --- call Jerry about soccer practice".
The cleanliness of the note is clean.

The memo is on the desk.
The description of the memo is "TOP SECRET: Major strides in Project Gietat.  Latest compound shows promise for long term emotion and perception modification in non-human primates.  Of 17 subjects, 14 responded favorably.  3 exhibited signs of severe disorientation, with increasing agitation and aggression as exposure continued.  Dr. Vitterog strongly advocates for human studies.  Dr. Dalle strongly objects.  Concern among team that Vitterog may proceed with self-experimentation.  Dr. Fiazzita expresses concern about possible airborne effects.".
The cleanliness of the memo is clean.

[Secret Door]
[source: http://inform7.com/extensions/Andrew%20Owen/Secret%20Doors/source.html]
[Reduced for simplicity]

A secret door is a kind of door.
A secret door is scenery.
A secret door is closed.
	
Instead of going through a secret door which is closed:
	say "you can't go that way."

Instead of doing something to a secret door which is closed:
	say "you don't see any such thing."

[Doors]

The west door is a door.
The west door is unlocked.
The west door is east of Classroom and west of Hallway.

The east door is a door.
The east door is locked.
The east door is west of Supply Closet and east of Hallway.

The south door is a door.
The south door is unlocked.
The south door is north of Laboratory and south of Hallway.

The secret passage is a secret door.
The cleanliness of the secret passage is clean.
The secret passage is unlocked and closed.
The secret passage is east of Secret Lab and west of Laboratory.

Before touching or cleaning the whiteboard:
	now the secret passage is open;
	say "When you touch the whiteboard, you accidentally activate a hidden mechanism - a secret passage opens to the west!"

[TEST]
Test me with "pick up notebook / go west / pick up textbook / pick up article / pick up trash can / drop trash can / go east / go south / take clock / take beaker / go north / unlock east door with key / go east / take rag / take pine sol / clean article / clean textbook / clean key / clean notebook / clean floor / clean west door / clean east door / clean south door / go west"
