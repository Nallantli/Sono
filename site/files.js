let files = [
    "datasetgen.so",
    "hello.so",
    "maptest.so",
    "ph.so"
];

let elem = document.getElementById("examples");
files.forEach((e) => {
    elem.innerHTML += `<button class="file" onclick="loadfile('${e}')">${e}</button>`;
});

function loadfile(filename) {
    socket.send("FILE\n" + filename)
}