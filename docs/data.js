const LIBRARIES = {
    "system": {
        name: "System Library",
        file: "system.so",
        import: [
            "LIB_Console"
        ],
        load: [
            "math"
        ],
        values: [],
        methods: [{
                name: "print",
                template: undefined,
                params: [{
                    modifier: "final",
                    key: "message",
                    type: [
                        "Any"
                    ]
                }],
                desc: "Sends a value to be printed by its <code>String</code> representation at standard output.",
                return: undefined,
                see: []
            },
            {
                name: "println",
                template: undefined,
                params: [{
                    modifier: "final",
                    key: "message",
                    type: [
                        "Any"
                    ]
                }],
                desc: "Sends a value to be printed by its <code>String</code> representation at standard output with a trailing newline.",
                return: undefined,
                see: ["system.print"]
            },
            {
                name: "exit",
                template: undefined,
                params: [],
                desc: "Exits the interpreter.",
                return: undefined,
                see: []
            },
            {
                name: "timeMS",
                template: undefined,
                params: [],
                desc: "Fetches the current time in milliseconds according to the system clock.",
                return: "Number",
                see: []
            },
            {
                name: "match",
                template: "String",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "String"
                        ]
                    },
                    {
                        modifier: "ref",
                        key: "pattern",
                        type: [
                            "String"
                        ]
                    }
                ],
                desc: "Finds all matches to a given regex pattern for the given <code>String</code>, as a <code>Vector</code> of indices of start and end positions for each match.",
                return: "Vector",
                see: []
            },
            {
                name: "log",
                template: "Number",
                params: [{
                    modifier: "ref",
                    key: "this",
                    type: [
                        "Number"
                    ]
                }],
                desc: "Returns the natural log of a given <code>Number</code>.",
                return: "Number",
                see: []
            },
            {
                name: "getNum",
                template: undefined,
                params: [{
                    modifier: "ref",
                    key: "x",
                    type: [
                        "Number",
                        "String"
                    ]
                }],
                desc: "Returns a <code>Number</code> value of the given input.",
                return: "Number",
                see: []
            },
            {
                name: "getStr",
                template: undefined,
                params: [{
                    modifier: "ref",
                    key: "x",
                    type: [
                        "Any"
                    ]
                }],
                desc: "Returns a <code>String</code> value of the given input.",
                return: "String",
                see: []
            },
            {
                name: "getVec",
                template: undefined,
                params: [{
                    modifier: "ref",
                    key: "x",
                    type: [
                        "String",
                        "Vector",
                        "Word"
                    ]
                }],
                desc: "Returns a <code>Vector</code> value of the given input.",
                return: "Vector",
                see: []
            },
            {
                name: "getLen",
                template: undefined,
                params: [{
                    modifier: "ref",
                    key: "x",
                    type: [
                        "String",
                        "Vector",
                        "Word",
                        "Matrix"
                    ]
                }],
                desc: "Returns a <code>Number</code> value of the given input's length.",
                return: "Number",
                see: []
            },
            {
                name: "getWord",
                template: undefined,
                params: [{
                    modifier: "ref",
                    key: "x",
                    type: [
                        "String",
                        "Vector",
                        "Word"
                    ]
                }],
                desc: "Returns a <code>Word</code> value of the given input.",
                return: "Word",
                see: []
            },
            {
                name: "getPhone",
                template: undefined,
                params: [{
                    modifier: "ref",
                    key: "x",
                    type: [
                        "String",
                        "Word"
                    ]
                }],
                desc: "Returns a <code>Phone</code> value of the given input.",
                return: "Phone",
                see: []
            },
            {
                name: "getChar",
                template: undefined,
                params: [{
                    modifier: "ref",
                    key: "x",
                    type: [
                        "Number"
                    ]
                }],
                desc: "Returns a <code>String</code> value of the given input's character code.",
                return: "String",
                see: []
            },
            {
                name: "getCode",
                template: undefined,
                params: [{
                    modifier: "ref",
                    key: "x",
                    type: [
                        "String"
                    ]
                }],
                desc: "Returns a <code>Number</code> value of the given input's character code.",
                return: "Number",
                see: []
            },
            {
                name: "getMat",
                template: undefined,
                params: [{
                    modifier: "ref",
                    key: "x",
                    type: [
                        "Matrix",
                        "Vector"
                    ]
                }],
                desc: "Returns a <code>Matrix</code> value of the given input's feature list.",
                return: "Number",
                see: []
            },
            {
                name: "getCom",
                template: undefined,
                params: [{
                    modifier: "ref",
                    key: "x",
                    type: [
                        "Vector"
                    ]
                }],
                desc: "Returns a <code>Matrix</code> value of the given inputs common features.",
                return: "Matrix",
                see: []
            }
        ],
        classes: [{
            name: "Random",
            modifier: "static",
            values: [],
            methods: [{
                    name: "float",
                    template: undefined,
                    params: [],
                    desc: "Returns a random value between [0, 1).",
                    return: "Number",
                    see: []
                },
                {
                    name: "int",
                    template: undefined,
                    params: [{
                            modifier: "ref",
                            key: "lower",
                            type: [
                                "Number"
                            ]
                        },
                        {
                            modifier: "ref",
                            key: "upper",
                            type: [
                                "Number"
                            ]
                        }
                    ],
                    desc: "Returns a random integer value between a given lower bound and upper bound (exclusive).",
                    return: "Number",
                    see: [
                        "system.Random.float",
                        "math.floor"
                    ]
                },
                {
                    name: "set",
                    template: undefined,
                    params: [{
                        modifier: "ref",
                        key: "vector",
                        type: [
                            "Vector"
                        ]
                    }],
                    desc: "Returns a random element from the contents of a given <code>Vector</code>.",
                    return: "Any",
                    see: [
                        "system.Random.int"
                    ]
                }
            ],
            classes: []
        }, {
            name: "Input",
            modifier: "static",
            values: [],
            methods: [{
                    name: "line",
                    template: undefined,
                    params: [],
                    desc: "Asks the user for an input via the standard input channel.",
                    return: "String",
                    see: []
                },
                {
                    name: "number",
                    template: undefined,
                    params: [],
                    desc: "Asks the user for an input via the standard input channel.",
                    return: "Number",
                    see: []
                }
            ],
            classes: []
        }]
    },
    "math": {
        name: "Mathematics and Number Library",
        file: "math.so",
        import: [],
        load: [],
        values: [],
        methods: [{
                name: "abs",
                template: "Number",
                params: [{
                    modifier: "ref",
                    key: "this",
                    type: [
                        "Number"
                    ]
                }],
                desc: "Returns the absolute value of a <code>Number</code>.",
                return: "Number",
                see: []
            },
            {
                name: "floor",
                template: "Number",
                params: [{
                    modifier: "ref",
                    key: "this",
                    type: [
                        "Number"
                    ]
                }],
                desc: "Returns the value of a <code>Number</code> rounded down to the nearest integer.",
                return: "Number",
                see: []
            },
            {
                name: "ceil",
                template: "Number",
                params: [{
                    modifier: "ref",
                    key: "this",
                    type: [
                        "Number"
                    ]
                }],
                desc: "Returns the value of a <code>Number</code> rounded up to the nearest integer.",
                return: "Number",
                see: []
            },
            {
                name: "round",
                template: "Number",
                params: [{
                    modifier: "ref",
                    key: "this",
                    type: [
                        "Number"
                    ]
                }],
                desc: "Returns the value of a <code>Number</code> rounded to the nearest integer.",
                return: "Number",
                see: ["math.floor", "math.ceil", "math.abs"]
            },
            {
                name: "format",
                template: "Number",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "Number"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "d",
                        type: [
                            "Number"
                        ]
                    }
                ],
                desc: "Returns a <code>String</code> value of the given <code>Number</code> formatted to show the given amount of significant figures.",
                return: "String",
                see: []
            }
        ],
        classes: [{
            name: "Math",
            modifier: "static",
            values: [],
            methods: [{
                    name: "min",
                    template: undefined,
                    params: [{
                        modifier: "ref",
                        key: "vector",
                        type: [
                            "Vector"
                        ]
                    }],
                    desc: "Returns the minimum value from a set of elements in a given <code>Vector</code>.",
                    return: "Number",
                    see: []
                },
                {
                    name: "minIndex",
                    template: undefined,
                    params: [{
                        modifier: "ref",
                        key: "vector",
                        type: [
                            "Vector"
                        ]
                    }],
                    desc: "Returns the index of the lowest element from a given <code>Vector</code>.",
                    return: "Number",
                    see: []
                },
                {
                    name: "add",
                    template: undefined,
                    params: [{
                            modifier: "ref",
                            key: "a",
                            type: [
                                "Number",
                                "Vector",
                                "Word",
                                "Matrix",
                                "String"
                            ]
                        },
                        {
                            modifier: "ref",
                            key: "b",
                            type: [
                                "Number",
                                "Vector",
                                "Word",
                                "Matrix",
                                "String"
                            ]
                        }
                    ],
                    desc: "Equivalent to the usage of the operator <code>+</code> between two values.",
                    return: "Any",
                    see: []
                },
                {
                    name: "sub",
                    template: undefined,
                    params: [{
                            modifier: "ref",
                            key: "a",
                            type: [
                                "Number"
                            ]
                        },
                        {
                            modifier: "ref",
                            key: "b",
                            type: [
                                "Number"
                            ]
                        }
                    ],
                    desc: "Equivalent to the usage of the operator <code>-</code> between two <code>Number</code> values.",
                    return: "Number",
                    see: []
                },
                {
                    name: "mul",
                    template: undefined,
                    params: [{
                            modifier: "ref",
                            key: "a",
                            type: [
                                "Number"
                            ]
                        },
                        {
                            modifier: "ref",
                            key: "b",
                            type: [
                                "Number"
                            ]
                        }
                    ],
                    desc: "Equivalent to the usage of the operator <code>*</code> between two <code>Number</code> values.",
                    return: "Number",
                    see: []
                },
                {
                    name: "mul",
                    template: undefined,
                    params: [{
                            modifier: "ref",
                            key: "a",
                            type: [
                                "Number"
                            ]
                        },
                        {
                            modifier: "ref",
                            key: "b",
                            type: [
                                "Number"
                            ]
                        }
                    ],
                    desc: "Equivalent to the usage of the operator <code>*</code> between two <code>Number</code> values.",
                    return: "Number",
                    see: []
                },
                {
                    name: "div",
                    template: undefined,
                    params: [{
                            modifier: "ref",
                            key: "a",
                            type: [
                                "Number"
                            ]
                        },
                        {
                            modifier: "ref",
                            key: "b",
                            type: [
                                "Number"
                            ]
                        }
                    ],
                    desc: "Equivalent to the usage of the operator <code>/</code> between two <code>Number</code> values.",
                    return: "Number",
                    see: []
                },
                {
                    name: "mod",
                    template: undefined,
                    params: [{
                            modifier: "ref",
                            key: "a",
                            type: [
                                "Number"
                            ]
                        },
                        {
                            modifier: "ref",
                            key: "b",
                            type: [
                                "Number"
                            ]
                        }
                    ],
                    desc: "Equivalent to the usage of the operator <code>%</code> between two <code>Number</code> values.",
                    return: "Number",
                    see: []
                },
                {
                    name: "pow",
                    template: undefined,
                    params: [{
                            modifier: "ref",
                            key: "a",
                            type: [
                                "Number"
                            ]
                        },
                        {
                            modifier: "ref",
                            key: "b",
                            type: [
                                "Number"
                            ]
                        }
                    ],
                    desc: "Equivalent to the usage of the operator <code>**</code> between two <code>Number</code> values.",
                    return: "Number",
                    see: []
                }
            ],
            classes: []
        }]
    },
    "hash": {
        name: "Hashing Library",
        file: "hash.so",
        import: [],
        load: [],
        values: [],
        methods: [{
            name: "hash",
            template: "String",
            params: [{
                    modifier: "ref",
                    key: "this",
                    type: [
                        "String"
                    ]
                },
                {
                    modifier: "ref",
                    key: "mod",
                    type: [
                        "Number"
                    ]
                }
            ],
            desc: "Returns a unique hash number for a given <code>String</code>, with an optional <code>Number</code> limit.",
            return: "Number",
            see: []
        }],
        classes: []
    },
    "feature": {
        name: "Feature Prototyping Method Library",
        file: "feature.so",
        import: [],
        load: ["string"],
        values: [],
        methods: [{
            name: "getPair",
            template: "Feature",
            params: [{
                modifier: "ref",
                key: "this",
                type: [
                    "Feature"
                ]
            }],
            desc: "Return a <code>Vector</code> pair of a given <code>Feature</code> value's quality (0) and key (1).",
            return: "Vector",
            see: ["string.split"]
        }],
        classes: []
    },
    "map": {
        name: "Map Library",
        file: "map.so",
        import: [],
        load: ["hash"],
        values: [],
        methods: [],
        classes: [{
            name: "Map",
            modifier: "struct",
            values: [{
                    key: "buckets",
                    desc: "Internal container for the <code>Map</code> object that maintains the hashed values as <code>Node</code> values.",
                    value: "null"
                },
                {
                    key: "size",
                    desc: "Amount of elements in the <code>Map</code>.",
                    value: "null"
                },
                {
                    key: "rawsize",
                    desc: "Actual size of the internal <code>bucket</code>.",
                    value: "null"
                }
            ],
            methods: [{
                    name: "init",
                    template: undefined,
                    params: [{
                        modifier: "ref",
                        key: "initVector",
                        type: [
                            "Vector"
                        ]
                    }],
                    desc: "Constructor for the <code>Map</code> object, taking an optional <code>Vector</code> literal to declare contents.",
                    return: undefined,
                    see: ["map.Map.getIndex"]
                },
                {
                    name: "isEmpty",
                    template: undefined,
                    params: [],
                    desc: "Returns whether the amount of elements in the <code>Map</code> is equal to zero.",
                    return: "Number",
                    see: []
                },
                {
                    name: "getIndex",
                    template: undefined,
                    params: [{
                        modifier: "ref",
                        key: "key",
                        type: [
                            "Any"
                        ]
                    }],
                    desc: "Retrieves the value of element from the <code>Map</code> at a given index.",
                    return: "Any",
                    see: ["map.Map.expand", "map.Map.Node", "hash.hash"]
                },
                {
                    name: "remove",
                    template: undefined,
                    params: [{
                        modifier: "ref",
                        key: "key",
                        type: [
                            "Any"
                        ]
                    }],
                    desc: "Removes the element of a given key value. Returns <code>true</code> if successful.",
                    return: "Number",
                    see: ["hash.hash"]
                },
                {
                    name: "containsKey",
                    template: undefined,
                    params: [{
                        modifier: "ref",
                        key: "key",
                        type: [
                            "Any"
                        ]
                    }],
                    desc: "Returns whether the <code>Map</code> contains an element assigned to the given key value.",
                    return: "Number",
                    see: ["hash.hash"]
                },
                {
                    name: "expand",
                    template: undefined,
                    params: [],
                    desc: "Doubles the interna size of the <code>Map</code> object's internal bucket <code>Vector</code>.",
                    return: undefined,
                    see: ["map.Map.getIndex"]
                },
                {
                    name: "getVec",
                    template: undefined,
                    params: [],
                    desc: "Returns a <code>Vector</code> representation of the contents of the <code>Map</code>.",
                    return: "Vector",
                    see: ["map.Map.Element"]
                },
                {
                    name: "getLen",
                    template: undefined,
                    params: [],
                    desc: "Returns the size of the <code>Map</code>.",
                    return: "Number",
                    see: []
                },
                {
                    name: "getStr",
                    template: undefined,
                    params: [],
                    desc: "Returns a <code>String</code> representation of the contents of the <code>Map</code>.",
                    return: "String",
                    see: ["map.Map.getVec"]
                }
            ],
            classes: [{
                    name: "Node",
                    modifier: "struct",
                    values: [{
                            key: "key",
                            desc: "Key of the <code>Node</code>.",
                            value: "null"
                        },
                        {
                            key: "value",
                            desc: "Value of the <code>Node</code>.",
                            value: "null"
                        },
                        {
                            key: "prev",
                            desc: "Previous <code>Node</code> in the hashed bucket.",
                            value: "null"
                        },
                        {
                            key: "next",
                            desc: "Following <code>Node</code> in the hashed bucket.",
                            value: "null"
                        }
                    ],
                    methods: [{
                        name: "init",
                        template: undefined,
                        params: [{
                                modifier: "ref",
                                key: "key",
                                type: [
                                    "Any"
                                ]
                            },
                            {
                                modifier: "ref",
                                key: "value",
                                type: [
                                    "Any"
                                ]
                            },
                            {
                                modifier: "ref",
                                key: "prev",
                                type: [
                                    "Node"
                                ]
                            }
                        ],
                        desc: "Constructor for the <code>Node</code> object.",
                        return: undefined,
                        see: []
                    }],
                    classes: []
                },
                {
                    name: "Element",
                    modifier: "struct",
                    values: [{
                            key: "key",
                            desc: "Key of the <code>Element</code>.",
                            value: "null"
                        },
                        {
                            key: "value",
                            desc: "Value of the <code>Element</code>.",
                            value: "null"
                        }
                    ],
                    methods: [{
                            name: "init",
                            template: undefined,
                            params: [{
                                    modifier: "ref",
                                    key: "key",
                                    type: [
                                        "Any"
                                    ]
                                },
                                {
                                    modifier: "ref",
                                    key: "value",
                                    type: [
                                        "Any"
                                    ]
                                }
                            ],
                            desc: "Constructor for the <code>Element</code> object, the general object used to retrieve and iterate values of a <code>Map</code>.",
                            return: undefined,
                            see: []
                        },
                        {
                            name: "getStr",
                            template: undefined,
                            params: [],
                            desc: "Returns a <code>String</code> representation of the contents of the <code>Element</code>.",
                            return: "String",
                            see: []
                        }
                    ],
                    classes: []
                }
            ]
        }]
    },
    "file": {
        name: "File Reading and Writing Library",
        file: "file.so",
        import: ["LIB_FileIO"],
        load: [],
        values: [],
        methods: [],
        classes: [{
            name: "File",
            modifier: "struct",
            values: [{
                key: "pointer",
                desc: "Internal memory address to the file processor.",
                value: "null"
            }],
            methods: [{
                    name: "init",
                    template: undefined,
                    params: [{
                        modifier: "ref",
                        key: "filename",
                        type: [
                            "String"
                        ]
                    }],
                    desc: "Constructor for the <code>File</code> object which accepts a path to a file in the form of a <code>String</code>.",
                    return: undefined,
                    see: []
                },
                {
                    name: "getReader",
                    template: undefined,
                    params: [],
                    desc: "Returns a <code>Reader</code> object for the file to have its contents parsed.",
                    return: "Reader",
                    see: ["file.File.Reader"]
                },
                {
                    name: "getWriter",
                    template: undefined,
                    params: [],
                    desc: "Returns a <code>Writer</code> object for the file allowing content to be written.",
                    return: "Reader",
                    see: ["file.File.Writer"]
                },
                {
                    name: "getStr",
                    template: undefined,
                    params: [],
                    desc: "Returns a <code>String</code> representation of the <code>File</code> object.",
                    return: "String",
                    see: []
                }
            ],
            classes: [{
                    name: "Reader",
                    modifier: "struct",
                    values: [{
                        key: "pointer",
                        desc: "Internal memory address to the file's reading manager.",
                        value: "null"
                    }],
                    methods: [{
                            name: "init",
                            template: undefined,
                            params: [{
                                modifier: "ref",
                                key: "pointer",
                                type: [
                                    "Pointer"
                                ]
                            }],
                            desc: "Constructor for the <code>Reader</code> object.",
                            return: undefined,
                            see: []
                        },
                        {
                            name: "readLine",
                            template: undefined,
                            params: [],
                            desc: "Reads and returns a single line from the file, giving <code>null</code> if the <code>Reader</code> finds no more content to be read.",
                            return: "String",
                            see: []
                        },
                        {
                            name: "read",
                            template: undefined,
                            params: [{
                                modifier: undefined,
                                key: "i",
                                type: [
                                    "Number"
                                ]
                            }],
                            desc: "Reads and returns the contents of a file up to a given amount of bytes, defaulting to a single byte if no value is given.",
                            return: "String",
                            see: []
                        },
                        {
                            name: "close",
                            template: undefined,
                            params: [],
                            desc: "Closes the <code>Reader</code> object and removes it from memory.",
                            return: undefined,
                            see: []
                        },
                        {
                            name: "getStr",
                            template: undefined,
                            params: [],
                            desc: "Returns a <code>String</code> representation of the of <code>Reader</code> object.",
                            return: "String",
                            see: []
                        }
                    ],
                    classes: []
                },
                {
                    name: "Writer",
                    modifier: "struct",
                    values: [{
                        key: "pointer",
                        desc: "Internal memory address to the file's writing manager.",
                        value: "null"
                    }],
                    methods: [{
                            name: "init",
                            template: undefined,
                            params: [{
                                modifier: "ref",
                                key: "pointer",
                                type: [
                                    "Pointer"
                                ]
                            }],
                            desc: "Constructor for the <code>Writer</code> object.",
                            return: undefined,
                            see: []
                        },
                        {
                            name: "write",
                            template: undefined,
                            params: [{
                                modifier: "ref",
                                key: "string",
                                type: [
                                    "String"
                                ]
                            }],
                            desc: "Writes a sequence of bytes to the <code>File</code>.",
                            return: undefined,
                            see: []
                        },
                        {
                            name: "close",
                            template: undefined,
                            params: [],
                            desc: "Closes the <code>Writer</code> object and removes it from memory.",
                            return: undefined,
                            see: []
                        },
                        {
                            name: "getStr",
                            template: undefined,
                            params: [],
                            desc: "Returns a <code>String</code> representation of the of <code>Writer</code> object.",
                            return: "String",
                            see: []
                        }
                    ],
                    classes: []
                }
            ]
        }]
    },
    "vector": {
        name: "Vector Prototyping Methods Library",
        file: "vector.so",
        import: [],
        load: ["math", "system"],
        values: [],
        methods: [{
                name: "pop",
                template: "Vector",
                params: [{
                    modifier: "ref",
                    key: "this",
                    type: [
                        "Vector"
                    ]
                }],
                desc: "Returns the given <code>Vector</code> with the final element removed.",
                return: "Vector",
                see: []
            },
            {
                name: "shift",
                template: "Vector",
                params: [{
                    modifier: "ref",
                    key: "this",
                    type: [
                        "Vector"
                    ]
                }],
                desc: "Returns the given <code>Vector</code> with the first element removed.",
                return: "Vector",
                see: []
            },
            {
                name: "front",
                template: "Vector",
                params: [{
                    modifier: "ref",
                    key: "this",
                    type: [
                        "Vector"
                    ]
                }],
                desc: "Returns the first element of the given <code>Vector</code>.",
                return: "Any",
                see: []
            },
            {
                name: "back",
                template: "Vector",
                params: [{
                    modifier: "ref",
                    key: "this",
                    type: [
                        "Vector"
                    ]
                }],
                desc: "Returns the last element of the given <code>Vector</code>.",
                return: "Any",
                see: []
            },
            {
                name: "isEmpty",
                template: "Vector",
                params: [{
                    modifier: "ref",
                    key: "this",
                    type: [
                        "Vector"
                    ]
                }],
                desc: "Returns <code>true</code> if the given <code>Vector</code> contains no elements.",
                return: "Number",
                see: []
            },
            {
                name: "contains",
                template: "Vector",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "Vector"
                        ]
                    },
                    {
                        modifier: "ref",
                        key: "value",
                        type: [
                            "Any"
                        ]
                    }
                ],
                desc: "Returns <code>true</code> if the given <code>Vector</code> contains the an element equivalent to <code>value</code>.",
                return: "Number",
                see: []
            },
            {
                name: "push",
                template: "Vector",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "Vector"
                        ]
                    },
                    {
                        modifier: "ref",
                        key: "value",
                        type: [
                            "Any"
                        ]
                    }
                ],
                desc: "Appends an element to the back of the <code>Vector</code>.",
                return: "Vector",
                see: []
            },
            {
                name: "join",
                template: "Vector",
                params: [{
                    modifier: "ref",
                    key: "this",
                    type: [
                        "Vector"
                    ]
                }],
                desc: "Returns the value of the <code>Vector</code>'s elements concatanated.",
                return: "String",
                see: []
            },
            {
                name: "remove",
                template: "Vector",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "Vector"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "index",
                        type: [
                            "Number"
                        ]
                    }
                ],
                desc: "Removes the element of a <code>Vector</code> at the given index.",
                return: "Vector",
                see: []
            },
            {
                name: "reverse",
                template: "Vector",
                params: [{
                    modifier: "ref",
                    key: "this",
                    type: [
                        "Vector"
                    ]
                }],
                desc: "Returns a <code>Vector</code> with the elements of the former in reversed order.",
                return: "Vector",
                see: []
            },
            {
                name: "distArray",
                template: "Vector",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "Vector"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "vector",
                        type: [
                            "Vector"
                        ]
                    },
                    {
                        modifier: undefined,
                        key: "costDel",
                        type: [
                            "Number"
                        ]
                    },
                    {
                        modifier: undefined,
                        key: "costIns",
                        type: [
                            "Number"
                        ]
                    },
                    {
                        modifier: undefined,
                        key: "costRep",
                        type: [
                            "Number"
                        ]
                    }
                ],
                desc: "Generates a two-dimensional <code>Vector</code> containing the structure expressed in a full graph of the Levenshtein distance calculation. The cost parameters default to <code>1</code> when left <code>null</code>.",
                return: "Vector",
                see: []
            },
            {
                name: "dist",
                template: "Vector",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "Vector"
                        ]
                    },
                    {
                        modifier: "ref",
                        key: "vector",
                        type: [
                            "Vector"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "costDel",
                        type: [
                            "Number"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "costIns",
                        type: [
                            "Number"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "costRep",
                        type: [
                            "Number"
                        ]
                    }
                ],
                desc: "Returns the calculated distance between two <code>Vector</code> values according to Levenshtein distance.",
                return: "Number",
                see: ["vector.distArray"]
            },
            {
                name: "steps",
                template: "Vector",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "Vector"
                        ]
                    },
                    {
                        modifier: "ref",
                        key: "vector",
                        type: [
                            "Vector"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "costDel",
                        type: [
                            "Number"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "costIns",
                        type: [
                            "Number"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "costRep",
                        type: [
                            "Number"
                        ]
                    }
                ],
                desc: "Returns a <code>Vector</code> list of the steps required to transform one <code>Vector</code> value into the other according to Levenshtein distance.",
                return: "Vector",
                see: ["vector.distArray", "vector.reverse"]
            },
            {
                name: "nGram",
                template: "Vector",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "Vector"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "n",
                        type: [
                            "Number"
                        ]
                    }
                ],
                desc: "Calculates the n-gram values for a <code>Vector</code> value's contents.",
                return: "Vector",
                see: []
            },
            {
                name: "sort",
                template: "Vector",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "Vector"
                        ]
                    },
                    {
                        modifier: undefined,
                        key: "comp",
                        type: [
                            "Function"
                        ]
                    }
                ],
                desc: "Sorts a given <code>Vector</code> according to the comparator <code>Function</code>, which defaults to numerical order when left <code>null</code>.",
                return: "Vector",
                see: []
            },
            {
                name: "map",
                template: "Vector",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "Vector"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "function",
                        type: [
                            "Function"
                        ]
                    }
                ],
                desc: "Maps a <code>Function</code> to each element in the <code>Vector</code> and returns the resultant <code>Vector</code>.",
                return: "Vector",
                see: []
            },
            {
                name: "filter",
                template: "Vector",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "Vector"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "function",
                        type: [
                            "Function"
                        ]
                    }
                ],
                desc: "Filters the elements of a <code>Vector</code> according to the parameters set by a given <code>Function</code>.",
                return: "Vector",
                see: []
            },
            {
                name: "foldRight",
                template: "Vector",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "Vector"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "function",
                        type: [
                            "Function"
                        ]
                    }
                ],
                desc: "Conducts the functional folding by the right on a given <code>Vector</code> according to the return values of the <code>Function</code>.",
                return: "Vector",
                see: ["vector.shift"]
            },
            {
                name: "foldLeft",
                template: "Vector",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "Vector"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "function",
                        type: [
                            "Function"
                        ]
                    }
                ],
                desc: "Conducts the functional folding by the left on a given <code>Vector</code> according to the return values of the <code>Function</code>.",
                return: "Vector",
                see: ["vector.pop"]
            },
            {
                name: "randomize",
                template: "Vector",
                params: [{
                    modifier: "ref",
                    key: "this",
                    type: [
                        "Vector"
                    ]
                }],
                desc: "Returns a <code>Vector</code> with values in a random arrangement.",
                return: "Vector",
                see: ["vector.remove", "system.Random.int"]
            }
        ],
        classes: []
    },
    "string": {
        name: "String Prototyping Methods Library",
        file: "string.so",
        import: [],
        load: ["system", "vector"],
        values: [],
        methods: [{
                name: "pop",
                template: "String",
                params: [{
                    modifier: "ref",
                    key: "this",
                    type: [
                        "String"
                    ]
                }],
                desc: "Returns the given <code>String</code> with the final character removed.",
                return: "String",
                see: ["vector.pop", "vector.join"]
            },
            {
                name: "shift",
                template: "String",
                params: [{
                    modifier: "ref",
                    key: "this",
                    type: [
                        "String"
                    ]
                }],
                desc: "Returns the given <code>String</code> with the first character removed.",
                return: "String",
                see: ["vector.shift", "vector.join"]
            },
            {
                name: "front",
                template: "String",
                params: [{
                    modifier: "ref",
                    key: "this",
                    type: [
                        "String"
                    ]
                }],
                desc: "Returns the first character of the given <code>String</code>.",
                return: "String",
                see: ["vector.front"]
            },
            {
                name: "back",
                template: "String",
                params: [{
                    modifier: "ref",
                    key: "this",
                    type: [
                        "String"
                    ]
                }],
                desc: "Returns the last character of the given <code>String</code>.",
                return: "String",
                see: ["vector.back"]
            },
            {
                name: "isEmpty",
                template: "String",
                params: [{
                    modifier: "ref",
                    key: "this",
                    type: [
                        "String"
                    ]
                }],
                desc: "Returns <code>true</code> if the given <code>String</code> contains no characters.",
                return: "Number",
                see: []
            },
            {
                name: "at",
                template: "String",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "String"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "index",
                        type: [
                            "Number"
                        ]
                    }
                ],
                desc: "Returns the character of the <code>String</code> at the given index.",
                return: "String",
                see: []
            },
            {
                name: "push",
                template: "String",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "String"
                        ]
                    },
                    {
                        modifier: "ref",
                        key: "value",
                        type: [
                            "String"
                        ]
                    }
                ],
                desc: "Returns the concatanation of two <code>String</code> values.",
                return: "String",
                see: []
            },
            {
                name: "contains",
                template: "String",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "String"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "pattern",
                        type: [
                            "String"
                        ]
                    }
                ],
                desc: "Returns <code>true</code> if the given <code>String</code> has contents that match the REGEX value of <code>pattern</code>.",
                return: "Number",
                see: ["system.match"]
            },
            {
                name: "distArray",
                template: "String",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "String"
                        ]
                    },
                    {
                        modifier: "ref",
                        key: "string",
                        type: [
                            "String"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "costDel",
                        type: [
                            "Number"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "costIns",
                        type: [
                            "Number"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "costRep",
                        type: [
                            "Number"
                        ]
                    }
                ],
                desc: "Generates a two-dimensional <code>Vector</code> containing the structure expressed in a full graph of the Levenshtein distance calculation. The cost parameters default to <code>1</code> when left <code>null</code>.",
                return: "Vector",
                see: ["vector.distArray"]
            },
            {
                name: "dist",
                template: "String",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "String"
                        ]
                    },
                    {
                        modifier: "ref",
                        key: "string",
                        type: [
                            "String"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "costDel",
                        type: [
                            "Number"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "costIns",
                        type: [
                            "Number"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "costRep",
                        type: [
                            "Number"
                        ]
                    }
                ],
                desc: "Returns the calculated distance between two <code>String</code> values according to Levenshtein distance.",
                return: "Number",
                see: ["vector.dist"]
            },
            {
                name: "steps",
                template: "String",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "String"
                        ]
                    },
                    {
                        modifier: "ref",
                        key: "string",
                        type: [
                            "String"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "costDel",
                        type: [
                            "Number"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "costIns",
                        type: [
                            "Number"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "costRep",
                        type: [
                            "Number"
                        ]
                    }
                ],
                desc: "Returns a <code>Vector</code> list of the steps required to transform one <code>String</code> value into the other according to Levenshtein distance.",
                return: "Vector",
                see: ["vector.steps"]
            },
            {
                name: "nGram",
                template: "String",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "String"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "n",
                        type: [
                            "Number"
                        ]
                    },
                    {
                        modifier: undefined,
                        key: "delim",
                        type: [
                            "String"
                        ]
                    }
                ],
                desc: "Calculates the n-gram values for a <code>String</code> value's contents, tokenized by a given delimiter.",
                return: "Vector",
                see: ["vector.nGram", "string.split"]
            },
            {
                name: "split",
                template: "String",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "String"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "pattern",
                        type: [
                            "String"
                        ]
                    }
                ],
                desc: "Splits a given <code>String</code> according to the given REGEX pattern.",
                return: "Vector",
                see: ["system.match", "vector.join"]
            },
            {
                name: "replace",
                template: "String",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "String"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "pattern",
                        type: [
                            "String"
                        ]
                    },
                    {
                        modifier: "final",
                        key: "value",
                        type: [
                            "String"
                        ]
                    }
                ],
                desc: "Replaces the contents of a given <code>String</code> according to the given REGEX pattern to a new value.",
                return: "String",
                see: ["system.match", "vector.join"]
            }
        ],
        classes: []
    },
    "rule": {
        name: "Rule and Rulesets Library",
        file: "rule.so",
        import: [],
        load: [],
        values: [],
        methods: [{
                name: "apply",
                template: "Vector",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "String"
                        ]
                    },
                    {
                        modifier: "ref",
                        key: "ruleset",
                        type: [
                            "Vector",
                            "Rule"
                        ]
                    }
                ],
                desc: "Applies a given set of rules or single rule to each element in a given <code>Vector</code>.",
                return: "Vector",
                see: []
            },
            {
                name: "apply",
                template: "Word",
                params: [{
                        modifier: "ref",
                        key: "this",
                        type: [
                            "String"
                        ]
                    },
                    {
                        modifier: "ref",
                        key: "ruleset",
                        type: [
                            "Vector",
                            "Rule"
                        ]
                    }
                ],
                desc: "Applies a given set of rules or single rule to a given <code>Word</code>.",
                return: "Word",
                see: []
            }
        ],
        classes: [{
            name: "RuleSets",
            modifier: "static",
            values: [{
                    key: "vowelNasalization",
                    desc: "<code>Rule</code> dictating the nasalization of vowels preceding a nasal consonant.",
                    value: `S |> [+|syl] -> [+|nasal] // null ~ [-|syl, +|nasal]`
                },
                {
                    key: "finalDevoicing",
                    desc: "<code>Rule</code> dictating the devoicing of a word-final consonant.",
                    value: `S |> [-|syl, +|cons, +|voice] -> [-|voice] // null ~ "#"`
                },
                {
                    key: "schwaEpenthesis",
                    desc: "<code>Rule</code> dictating the insertion of the phone [ə] between two stridents.",
                    value: `S |> null -> 'ə' // [-|syl, +|cons, +|str] ~ [-|syl, +|cons, +|str]`
                },
                {
                    key: "highVowelPalatalization",
                    desc: "<code>Rule</code> dictating the palatalization of a consonant before a high front vowel.",
                    value: `S |> [-|syl] -> [+|DOR, +|high, -|low, +|front, -|back] // null ~ [+|syl, +|high, +|front]`
                },
                {
                    key: "yodPalatalization",
                    desc: "<code>Rule</code> dictating the palatalization of a consonant before the phone [j].",
                    value: `Af |> [-|syl] -> [+|DOR, +|high, -|low, +|front, -|back] // "$" ~ 'j'`
                },
                {
                    key: "wLabialization",
                    desc: "<code>Rule</code> dictating the labialization of a consonant before the phone [w].",
                    value: `Af |> [-|syl] -> [+|LAB, +|round] // "$" ~ 'w'`
                },
                {
                    key: "medialVoicing",
                    desc: "<code>Rule</code> dictating the voicing of a consonant between two voiced vowels (Q: Aren't all vowels voiced? A: [soːd_ʑanaidesɯ̥])",
                    value: `S |> [-|syl, -|voice, +|cons] -> [+|voice] // [+|syl, +|voice] ~ [+|syl, +|voice]`
                },
                {
                    key: "medialFrication",
                    desc: "<code>Rule</code> dictating the frication of some consonants between two voiced vowels",
                    value: `S |> [-|syl, +|cons, -|son, -|del] -> [+|del, +|cont, +|str] // [+|syl, +|voice] ~ [+|syl, +|voice]`
                },
                {
                    key: "hDropping",
                    desc: "<code>Rule</code> dictating the elision of the phone [h] syllable initially.",
                    value: `S |> 'h' -> null // "$" ~ [+|syl]`
                },
                {
                    key: "nasalAssimilation",
                    desc: "<code>Rule</code> dictating the assimilation of the phone [n] to the place of the following consonant.",
                    value: `S |> 'n' -> [2|LAB, 2|round, 2|ld, 2|COR, 2|ant, 2|dist, 2|DOR, 2|high, 2|low, 2|front, 2|back] // null ~ [-|syl, +|cons, -|cont, 2|LAB, 2|round, 2|ld, 2|COR, 2|ant, 2|dist, 2|DOR, 2|high, 2|low, 2|front, 2|back]`
                },
                {
                    key: "deRhoticizationCons",
                    desc: "<code>Rule</code> dictating the elision of rhotic consonants before a consonant.",
                    value: `Af |> [+|syl] -> [+|long] // null ~ {[-|syl, +|son, -|nasal, -|LAB, -|LAT, -|DOR], "$", [-|syl]}`
                },
                {
                    key: "deRhoticizationFinal",
                    desc: "<code>Rule</code> dictating the elision of rhotic consonants at the end of a word.",
                    value: `Af |> [+|syl] -> [+|long] // null ~ {[-|syl, +|son, -|nasal, -|LAB, -|LAT, -|DOR], "#"}`
                },
                {
                    key: "stopDeletion",
                    desc: "<code>Rule</code> dictating the elision of the first plosive consonant in a sequence of two consecutive plosives.",
                    value: `S |> [-|son, -|del] -> null // null ~ {"$", [-|son, -|del]}`
                },
                {
                    key: "Common",
                    desc: "<code>Vector</code> containing rules commonly found in the realization or phonetic changes of the world's languages.",
                    value: `{
						vowelNasalization,
						finalDevoicing,
						schwaEpenthesis,
						highVowelPalatalization,
						yodPalatalization,
						wLabialization,
						medialVoicing,
						medialFrication,
						hDropping,
						nasalAssimilation,
						deRhoticizationCons,
						deRhoticizationFinal,
						stopDeletion
					}`
                }
            ],
            methods: [],
            classes: []
        }]
    }
}