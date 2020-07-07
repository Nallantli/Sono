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
                return: undefined
            },
            {
                name: "timeMS",
                template: undefined,
                params: [],
                desc: "Fetches the current time in milliseconds according to the system clock.",
                return: "Number"
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
                return: "Vector"
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
                return: "Number"
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
                return: "Number"
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
                return: "String"
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
                return: "Vector"
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
                return: "Number"
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
                return: "Word"
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
                return: "Phone"
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
                return: "String"
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
                return: "Number"
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
                return: "Number"
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
                return: "Matrix"
            }
        ],
        classes: [{
            name: "Random",
            modifier: "static",
            methods: [{
                    name: "float",
                    template: undefined,
                    params: [],
                    desc: "Returns a random value between [0, 1).",
                    return: "Number"
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
            ]
        }, {
            name: "Input",
            modifier: "static",
            methods: [{
                    name: "line",
                    template: undefined,
                    params: [],
                    desc: "Asks the user for an input via the standard input channel.",
                    return: "String"
                },
                {
                    name: "number",
                    template: undefined,
                    params: [],
                    desc: "Asks the user for an input via the standard input channel.",
                    return: "Number"
                }
            ]
        }]
    }
}