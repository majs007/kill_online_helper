/**************************************************************************
   Copyright (c) 2017 sewenew

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 *************************************************************************/

#ifndef SEWENEW_REDISPLUSPLUS_TEST_SCRIPT_CMDS_TEST_HPP
#define SEWENEW_REDISPLUSPLUS_TEST_SCRIPT_CMDS_TEST_HPP

#include <list>
#include <vector>
#include "utils.h"

namespace sw {

    namespace redis {

        namespace test {

            template<typename RedisInstance>
            void ScriptCmdTest<RedisInstance>::run() {
                cluster_specializing_test(*this,
                                          &ScriptCmdTest<RedisInstance>::_run,
                                          _redis);
            }

            template<typename RedisInstance>
            void ScriptCmdTest<RedisInstance>::_run(Redis &instance) {
                auto key1 = test_key("k1");
                auto key2 = test_key("k2");

                KeyDeleter<Redis> deleter(instance, {key1, key2});

                std::string script = "redis.call('set', KEYS[1], 1);"
                                     "redis.call('set', KEYS[2], 2);"
                                     "local first = redis.call('get', KEYS[1]);"
                                     "local second = redis.call('get', KEYS[2]);"
                                     "return first + second";

                auto num = instance.eval<long long>(script, {key1, key2}, {});
                REDIS_ASSERT(num == 3, "failed to test scripting for cluster");

                script = "return 1";
                num = instance.eval<long long>(script, {}, {});
                REDIS_ASSERT(num == 1, "failed to test eval");

                auto script_with_args = "return {ARGV[1] + 1, ARGV[2] + 2, ARGV[3] + 3}";
                std::vector<long long> res;
                instance.eval(script_with_args,
                              {"k"},
                              {"1", "2", "3"},
                              std::back_inserter(res));
                REDIS_ASSERT(res == std::vector<long long>({2, 4, 6}),
                             "failed to test eval with array reply");

                auto sha1 = instance.script_load(script);
                num = instance.evalsha<long long>(sha1, {}, {});
                REDIS_ASSERT(num == 1, "failed to test evalsha");

                auto sha2 = instance.script_load(script_with_args);
                res.clear();
                instance.evalsha(sha2,
                                 {"k"},
                                 {"1", "2", "3"},
                                 std::back_inserter(res));
                REDIS_ASSERT(res == std::vector<long long>({2, 4, 6}),
                             "failed to test evalsha with array reply");

                std::list<bool> exist_res;
                instance.script_exists({sha1, sha2, std::string("not exist")},
                                       std::back_inserter(exist_res));
                REDIS_ASSERT(exist_res == std::list<bool>({true, true, false}),
                             "failed to test script exists");

                instance.script_flush();
                exist_res.clear();
                instance.script_exists({sha1, sha2, std::string("not exist")},
                                       std::back_inserter(exist_res));
                REDIS_ASSERT(exist_res == std::list<bool>({false, false, false}),
                             "failed to test script flush");
            }

        }

    }

}

#endif // end SEWENEW_REDISPLUSPLUS_TEST_SCRIPT_CMDS_TEST_HPP
