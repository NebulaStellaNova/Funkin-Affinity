
//var MusicBeatState = Java.type("com.example.fnfaffinity.backend.utils.MusicBeatState");

function create() {
    //CurrentState.switchModState("TestMenu");
}

function scrollMenu(event) {
    // event.getInt("id");
}

function selectMenu(event) {
    // event.getInt("id");
    // event.getString("name");
    switch (event.getString("name")) {
        case "options":
            //event.put("cancelled", true);
            switchState(FreeplayState);
            break;
    }
}