package com.netflix.spinnaker.clouddriver.mhc.cache

class Keys {

    static enum Namespace {
        MHC_TEST,
        ANOTHER_TEST

        static String provider = 'mhc'

        final String ns

        private Namespace() {
            def parts = name().split('_')
            ns = parts.tail().inject(new StringBuilder(parts.head().toLowerCase())) {
                val, next -> val.append(next.charAt(0)).append(next.substring(1).toLowerCase())
            }
        }

        String toString() {
            ns
        }
    }

    static Map<String, String> parse(String key) {
        def parts = key.split(':')
        if (parts.length < 2) {
            return null
        }

        def result = [provider: parts[0], type: parts[1]]

        if (result.provider != Namespace.provider) {
            return null
        }

        switch (result.type) {
            case Namespace.MHC_TEST:
                result << [account: parts[2], some: parts[3]]
                break
            case Namespace.ANOTHER_TEST:
                result << [account: parts[2], another: parts[3]]
                break
            default:
                return null
                break
        }
        result
    }

    static String getSomeKey(String account, String some) {
        "${Namespace.provider}:${Namespace.MHC_TEST}:${account}:${some}"
    }

    static String getAnotherKey(String account, String another) {
        "${Namespace.provider}:${Namespace.MHC_TEST}:${account}:${another}"
    }

}
