let socket = new WebSocket("ws://sonolang.com:7777");

let past_commands = [];
let index = 0;
let init = false;

function initialize() {
    init = true;
    document.getElementById("banner").style.opacity = 1;
    setTimeout(() => {
        document.getElementById("banner").style.opacity = 0;
        document.getElementById("banner").style.pointerEvents = "none";
    }, this.animationDelay + 20);
}

socket.onopen = function(e) {
    console.log("[open] Connection established");
}

socket.onmessage = function(event) {
    if (!init) {
        initialize();
    }
    let s = event.data.split("\n");
    let header = s[0];
    s.shift();
    let message = s.join("\n");
    if (header == "OUT") {
        writeOutput(message);
    } else if (header == "FILE") {
        createTab(message);
    }
}

socket.onclose = function(event) {
    if (event.wasClean) {
        console.log(`[close] Connection closed cleanly, code=${event.code} reason=${event.reason}`);
    } else {
        console.log('[close] Connection died');
    }
}

socket.onerror = function(error) {
    console.log(`[error] ${error.message}`);
}

function sendInput(elem) {
    if (event.key == 'Enter') {
        var s = elem.innerHTML;
        past_commands.push(s);
        index = 0;
        s = s.replace(/\<br\>$/, "");
        s = s.split(/\&lt\;/g).join("<");
        s = s.split(/\&gt\;/g).join(">");
        s = s.split(/\&amp\;/g).join("&");
        socket.send("CODE\n" + s);
        elem.innerHTML = "";
    } else if (event.key == 'ArrowUp') {
        if (past_commands.length > index) {
            index++;
            elem.innerHTML = past_commands[past_commands.length - index];
        }
    } else if (event.key == 'Escape') {
        elem.innerHTML = "";
        index = 0;
    }
}

function runCode() {
    socket.send("CODE\n" + editor.getValue());
}

function writeOutput(string) {
    var elem = document.querySelector('#input');
    var newString = "";
    for (let i = 0; i < string.length; i++) {
        if (string.charAt(i) == "\n")
            newString += "<br>";
        else if (string.charAt(i) == "\t")
            newString += "&nbsp;&nbsp;&nbsp;&nbsp;";
        else
            newString += string.charAt(i);
    }
    elem.insertAdjacentHTML("beforebegin", newString);
    document.querySelector("#output").scrollTop = document.querySelector("#output").scrollHeight;
}

focusMethod = function getFocus() {
    var elem = document.getElementById("input");
    var x = document.getElementById("output").scrollLeft,
        y = document.getElementById("output").scrollTop;
    elem.focus();
    document.getElementById("output").scrollTo(x, y);
}

function toggleElement(id) {
    var x = document.getElementById(id);
    if (x.style.display === "none") {
        x.style.display = "block";
    } else {
        x.style.display = "none";
    }
}