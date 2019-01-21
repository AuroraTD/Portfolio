function update() {
	x = Math.floor((Math.random()*13)+1)-3;
	y = Math.floor((Math.random()*13)+1)-3;
	game_object.move(x,y);
	print(game_object.toString() + '\n');
}
