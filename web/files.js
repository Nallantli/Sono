let files = [
	"SOE.so",
	"threads.so",
	"transformer.so",
    "datasetgen.so",
    "deu.so",
    "epenthesis.so",
    "fibonacci.so",
    "findnatural.so",
    "hello.so",
    "hilo.so",
    "jpn.so",
    "maptest.so",
    "nah.so",
    "naturalsets.so",
    "nn_adder.so"
];

let fileCache = { "untitled.so": "" };
let tablist = ["untitled.so"];
let currFile = "untitled1.so";
let c = 1;

let elem = document.getElementById("examples");
files.forEach((e) => {
    elem.innerHTML += `<button class="file" onclick="loadfile('${e}')"><i class="fas fa-file-code"></i>${e}</button>`;
});

function loadfile(filename) {
    socket.send("FILE\n" + filename);
    currFile = filename
    toggleElement("examples");
}

function newTab() {
    c++;
    createTab("");
}

function createTab(content) {
    fileCache[currFile] = content;
    if (document.getElementById(`${currFile}`) == undefined) {
        let tabs = document.getElementById("tabs");
        tabs.innerHTML = tabs.innerHTML.trim() + `<li class="tablinks" id="${currFile}"><button class="opentab" onclick="openTab('${currFile}')"><i class="fas fa-file-code"></i>${currFile}</button><button class="closetab" onclick="closeTab('${currFile}')"><i class="fas fa-times-circle"></i></button></li>`;
    }
    if (tablist.indexOf(currFile) == -1) {
        tablist.push(currFile);
    }
    openTab(currFile);
    currFile = "untitled" + c + ".so";
}

function openTab(filename) {
    let selected = document.getElementsByClassName("selectedtab");
    fileCache[selected[0].id] = editor.getValue();

    let list = document.getElementsByClassName("tablinks");
    for (var i = 0; i < list.length; i++) {
        list[i].classList.remove("selectedtab");
    }

    document.getElementById(`${filename}`).classList.add("selectedtab");
    document.getElementById(`${filename}`).scrollIntoView();
    editor.setValue(fileCache[filename], -1);
}

function closeTab(filename) {
    if (tablist.length > 1) {
        let selected = document.getElementsByClassName("selectedtab");
        if (selected[0].id == filename) {
            let open = tablist[tablist.indexOf(filename) - 1];
            if (open == undefined)
                open = tablist[tablist.indexOf(filename) + 1];
            openTab(open);
        }
        tablist.splice(tablist.indexOf(filename), 1);
        let r = document.getElementById(filename);
        r.parentElement.removeChild(r);
    }
}