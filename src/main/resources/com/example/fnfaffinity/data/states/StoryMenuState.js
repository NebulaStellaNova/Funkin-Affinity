var daSprite

function add(sprite) {
    Main.add(sprite)
    return
}

function create() {
    print("hi")
    daSprite = new NovaSprite("menus/title/bg", 0, 0)
    add(daSprite)
}
