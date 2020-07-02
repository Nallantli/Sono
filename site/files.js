let files = [
    "datasetgen.so",
    "hello.so",
    "maptest.so",
    "deu.so",
    "epenthesis.so",
    "jpn.so",
    "nah.so",
    "naturalsets.so",
    "findnatural.so"
];

let elem = document.getElementById("examples");
files.forEach((e) => {
    elem.innerHTML += `<button class="file" onclick="loadfile('${e}')"><i class="fas fa-file-code"></i>${e}</button>`;
});

function loadfile(filename) {
    socket.send("FILE\n" + filename);
    toggleElement("examples");
}