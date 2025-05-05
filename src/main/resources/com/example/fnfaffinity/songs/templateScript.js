// NOTE: Do NOT delete this script, PlayState will NOT function correctly without this.

/*
    // Variables \\
    iconOpponent
    iconPlayer
    curStep
    curBeat
    downscroll
    botplay
    curVariation
    difficulty
    paused
    characters
    strumLines
    notes
    curSong

*/

// Create Related \\
function create() {
   //print("hello"); 
}

function postCreate() {
    /*
    var sprite = new NovaSprite(spritePath, x, y);
    add(sprite);
    */
}

// Update Related \\
function update() {
}

function postUpdate() {

}

// MusicBeatState Related \\
function beatHit() {
    curBeat;
}

function stepHit() {
    curStep;
}


// Pause Related \\
function onPause(event) {
    event.put("cancelled", false);
}

function onResume(event) {
    event.put("cancelled", false);
}

function onRestart(event) {
    event.put("cancelled", false);
}

function onExitToMenu(event) {
    event.put("cancelled", false);
}

// Note Related \\
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

// Song Event Related \\
function onEvent(event) {
    var eventName = event.getString("name");
    var eventParameters = event.getJSONArray("params");

    event.put("cancelled", false);
    /*
        Event Param Usage
            var param1 = eventParameters.getString(0); // Inter-changeable (getString, getInt, getFloat, getBoolean)
    */
}