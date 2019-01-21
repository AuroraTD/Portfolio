function update() {
	s = Math.random();
	x = Math.floor((Math.random()*13)+1)-3;
	y = Math.floor((Math.random()*13)+1)-3;
	if(s < 0.5) {
		game_object_1.move(x,y);
		print(game_object_1.toString() + '\n');
	}
	else {
		game_object_2.move(x,y);
		print(game_object_2.toString() + '\n');
	}
}
