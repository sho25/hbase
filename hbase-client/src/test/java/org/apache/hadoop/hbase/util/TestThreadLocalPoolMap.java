begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Random
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutionException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadLocalRandom
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseClassTestRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|testclassification
operator|.
name|MiscTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|testclassification
operator|.
name|SmallTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|PoolMap
operator|.
name|PoolType
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestThreadLocalPoolMap
extends|extends
name|PoolMapTestBase
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestThreadLocalPoolMap
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|protected
name|PoolType
name|getPoolType
parameter_list|()
block|{
return|return
name|PoolType
operator|.
name|ThreadLocal
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSingleThreadedClient
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|Random
name|rand
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
decl_stmt|;
name|String
name|randomKey
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|rand
operator|.
name|nextInt
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|randomValue
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|rand
operator|.
name|nextInt
argument_list|()
argument_list|)
decl_stmt|;
comment|// As long as the pool is not full, we should get back what we put
name|runThread
argument_list|(
name|randomKey
argument_list|,
name|randomValue
argument_list|,
name|randomValue
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|poolMap
operator|.
name|size
argument_list|(
name|randomKey
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMultiThreadedClients
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|Random
name|rand
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
decl_stmt|;
comment|// As long as the pool is not full, we should get back what we put
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|POOL_SIZE
condition|;
name|i
operator|++
control|)
block|{
name|String
name|randomKey
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|rand
operator|.
name|nextInt
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|randomValue
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|rand
operator|.
name|nextInt
argument_list|()
argument_list|)
decl_stmt|;
name|runThread
argument_list|(
name|randomKey
argument_list|,
name|randomValue
argument_list|,
name|randomValue
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|poolMap
operator|.
name|size
argument_list|(
name|randomKey
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|String
name|randomKey
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|rand
operator|.
name|nextInt
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|POOL_SIZE
condition|;
name|i
operator|++
control|)
block|{
name|String
name|randomValue
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|rand
operator|.
name|nextInt
argument_list|()
argument_list|)
decl_stmt|;
name|runThread
argument_list|(
name|randomKey
argument_list|,
name|randomValue
argument_list|,
name|randomValue
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|i
operator|+
literal|1
argument_list|,
name|poolMap
operator|.
name|size
argument_list|(
name|randomKey
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testPoolCap
parameter_list|()
throws|throws
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|Random
name|rand
init|=
name|ThreadLocalRandom
operator|.
name|current
argument_list|()
decl_stmt|;
name|String
name|randomKey
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|rand
operator|.
name|nextInt
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|POOL_SIZE
operator|*
literal|2
condition|;
name|i
operator|++
control|)
block|{
name|String
name|randomValue
init|=
name|String
operator|.
name|valueOf
argument_list|(
name|rand
operator|.
name|nextInt
argument_list|()
argument_list|)
decl_stmt|;
comment|// as of HBASE-4150, pool limit is no longer used with ThreadLocalPool
name|runThread
argument_list|(
name|randomKey
argument_list|,
name|randomValue
argument_list|,
name|randomValue
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
name|POOL_SIZE
operator|*
literal|2
argument_list|,
name|poolMap
operator|.
name|size
argument_list|(
name|randomKey
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

