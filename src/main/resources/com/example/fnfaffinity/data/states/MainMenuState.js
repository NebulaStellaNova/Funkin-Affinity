function scrollMenu(event) {
    // event.getInt("id");
}

function selectMenu(event) {
    // event.getInt("id");
    // event.getString("name");
    switch (event.getString("name")) {
        case "options":
            //event.put("cancelled", true);
            switchModState("TestMenu");
            break;
    }
}