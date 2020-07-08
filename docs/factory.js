body = document.getElementById("cont");
nav = document.getElementById("nav");

function writeValue(parent, v) {
    let s = `<tr class="value">`;
    s += `<td class="value_key">${v.key}</td>`;
    s += `<td class="value_desc">${v.desc}</td>`;
    s += `<td class="value_value">${v.value}</td>`;
    s += `</tr>`;
    return s;
}

function writeMethod(parent, m) {
    let s = `<a style="height:65px; margin-top: -65px; display:block;" name="${parent}.${m.name}"></a>`
    nav_s += `<p class="nav_method_link"><a href="#${parent}.${m.name}">${m.name}()</a></p>`;
    s += `<div class="method">`;
    s += `<div class="method_head">`;
    if (m.template != undefined) {
        s += `<span class="template">${m.template}::</span>`;
    }
    s += `<span class="method_name">${m.name}(`;
    let a = [];
    m.params.forEach((p) => {
        let s2 = ""
        if (p.modifier != undefined) {
            s2 += `<span class="param_modifier">${p.modifier} </span>`;
        }
        s2 += `<span class="param_key">${p.key}</span>`;
        a.push(s2);
    })
    if (a.length > 0) {
        s += `${a.join(", ")}`;
    }
    s += `)`;
    if (m.return != undefined) {
        s += `<span class="return_arrow"> => </span><span class="return_type">${m.return}</span>`;
    }
    s += `</span></div>`;

    if (m.params.length > 0) {
        s += `<table class="param_types">`;
        m.params.forEach((p) => {
            s += `<tr>`;
            s += `<td class="param_type_key">${p.key}</td><td class="param_type_type">`;
            p.type.forEach((t) => {
                s += `<p>${t}</p>`;
            });
            s += `</td></tr>`;
        })
        s += `</table>`;
    }

    s += `<div class="method_body">`;
    s += `<p>${m.desc}</p>`;
    if (m.see.length > 0) {
        s += `<div class="see_also"><p style="font-style: italic;">Confer:</p>`;
        m.see.forEach((l) => {
            s += `<p><a class="see_link" href="#${l}">${l}()</a></p>`;
        });
        s += `</div>`
    }
    s += `</div>`;
    s += `</div>`;
    return s;
}

function writeClass(parent, c) {
    let s = `<a style="height:65px; margin-top: -65px; display:block;" name="${parent}.${c.name}"></a>`;
    nav_s += `<details><summary class="nav_class_link"><a href="#${parent}.${c.name}">${c.name}</a></summary>`;
    s += `<div class="class">`;
    s += `<div class="class_head">`;
    s += `<span class="class_modifier">${c.modifier} </span>`;
    s += `<span class="class_name">${c.name} </span><span class="class_dec">class</span>`;
    s += `</div><div class="class_body">`;
    s += writeObject(parent + "." + c.name, c);
    s += `</div></div>`;
    nav_s += `</details>`;
    return s;
}

function writeObject(parent, o) {
    s = "";
    if (o.values.length > 0) {
        s += `<table class="values">`;
        o.values.forEach((v) => {
            s += writeValue(parent, v);
        });
        s += `</table>`;
    }

    if (o.methods.length > 0) {
        s += `<div class="methods">`;
        o.methods.forEach((m) => {
            s += writeMethod(parent, m);
        });
        s += `</div>`;
    }

    if (o.classes.length > 0) {
        s += `<div class="classes">`;
        o.classes.forEach((c) => {
            s += writeClass(parent, c);
        });
        s += `</div>`;
    }

    return s;
}

let nav_s = "";
let s = "";

Object.entries(LIBRARIES).forEach(([key, lib]) => {
    s += `<div id="lib-${key}">`;
    s += `<a style="height:0; display:block;" name="${key}"></a>`;
    nav_s += `<details><summary class="nav_lib_link"><a href="#${key}">lib/${key}</a></summary>`;
    s += `<div class="lib_key"><p>lib/${key}</p></div>`;
    s += `<div class="metadata">`;
    s += `<p class="lib_name">${lib.name}</p>`;
    s += `<p class="lib_file">${lib.file}</p>`;
    s += `<div class="imports">`;
    lib.import.forEach((i) => {
        s += `<p><span class="import">import </span><span class="import_name">"${i}"</span></p>`;
    });
    s += `</div>`;
    s += `<div class="loads">`;
    lib.load.forEach((i) => {
        s += `<p><span class="load">load </span><a href="#${i}" class="load_name">"${i}"</a></p>`;
    });
    s += `</div></div>`;

    s += writeObject(key, lib);

    s += `</div>`;
    nav_s += `</details>`
});

body.innerHTML = s;
nav.innerHTML = nav_s;