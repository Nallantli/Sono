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
                    desc: "Actual size of the interal <code>bucket</code>.",
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
    }
}