let socket = new WebSocket("wss://3.128.186.184:80/ws");

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

function runCode() {
    socket.send(editor.getValue());
}

function writeOutput(string) {
    var elem = document.querySelector('#output');
    var newString = "";
    for (let i = 0; i < string.length; i++) {
        if (string.charAt(i) == "\n")
            newString += "<br>";
        else if (string.charAt(i) == "\t")
            newString += "&nbsp;&nbsp;&nbsp;&nbsp;";
        else
            newString += string.charAt(i);
    }
    elem.innerHTML += newString;
}