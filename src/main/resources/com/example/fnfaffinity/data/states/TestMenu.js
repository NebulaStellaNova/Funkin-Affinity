function create() {
    var daSprite = new NovaSprite("menus/title/bg", 0, 0);
    add(daSprite);
}

function update() {
    if (NovaKeys.BACK_SPACE.justPressed) {
        switchState(MainMenuState);
    }
}

