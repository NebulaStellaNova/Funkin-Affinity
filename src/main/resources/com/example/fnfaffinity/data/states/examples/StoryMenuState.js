var daSprite

function add(sprite) {
    Main.add(sprite)
    return
}

function postCreate() {
    var Main = Java.type("com.example.fnfaffinity.Main")
    print("hi")
    daSprite = new NovaSprite("menus/title/bg", 0, 0)
    Main.add(daSprite)
}
