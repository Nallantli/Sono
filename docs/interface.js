let socket = new WebSocket("ws://3.128.186.184:80");

let past_commands = [];
let index = 0;

socket.onopen = function(e) {
    console.log("[open] Connection established");
}

socket.onmessage = function(event) {
    writeOutput(event.data);
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
        s = s.replace(/\&lt\;/, "<");
        s = s.replace(/\&gt\;/, ">");
        s = s.replace(/\&amp\;/, "&");
        socket.send(s);
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
    socket.send(editor.getValue());
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
    document.getElementById("input").focus();
}