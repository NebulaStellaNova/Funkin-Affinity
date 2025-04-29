function create() {
   print("hello"); 
}

function postCreate() {
    /*
    var sprite = new NovaSprite(spritePath, x, y);  // Seems to only sometimes work, not sure why.
    Main.add(sprite);                               // The only thing that seems to work in scripts consistantly,
    */                                              // are JSONObjects.
}

function update() {

}

function postUpdate() {

}

function beatHit() {
    curBeat;
}

function stepHit() {
    curStep;
}

function onNoteHit(event) {
    var direction = event.getInt("direction");
    var strumLineID = event.getInt("strumLineID");
    var noteType = event.getString("noteType");
    var noteTypeID = event.getInt("noteTypeID");
    var isSustainNote = event.getBoolean("isSustainNote");


    event.put("cancelled", false);
}

function onNoteMiss(event) {
    var direction = event.getInt("direction");
    var strumLineID = event.getInt("strumLineID");

    event.put("cancelled", false);
}

function onEvent(event) {
    var eventName = event.getString("name");
    var eventParameters = event.getJSONArray("params");

    event.put("cancelled", false);
    /*
        Event Param Usage
            var param1 = eventParameters.getString(0); // Inter-changeable (getString, getInt, getFloat, getBoolean)
    */
}