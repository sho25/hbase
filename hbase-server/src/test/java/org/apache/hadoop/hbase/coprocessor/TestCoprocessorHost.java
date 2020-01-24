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
name|coprocessor
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotNull
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
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
name|conf
operator|.
name|Configuration
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
name|Abortable
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
name|Coprocessor
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
name|CoprocessorEnvironment
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
name|HBaseConfiguration
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
name|junit
operator|.
name|Assert
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
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestCoprocessorHost
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
name|TestCoprocessorHost
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * An {@link Abortable} implementation for tests.    */
specifier|private
specifier|static
class|class
name|TestAbortable
implements|implements
name|Abortable
block|{
specifier|private
specifier|volatile
name|boolean
name|aborted
init|=
literal|false
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{
name|this
operator|.
name|aborted
operator|=
literal|true
expr_stmt|;
name|Assert
operator|.
name|fail
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
name|this
operator|.
name|aborted
return|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDoubleLoadingAndPriorityValue
parameter_list|()
block|{
specifier|final
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
specifier|final
name|String
name|key
init|=
literal|"KEY"
decl_stmt|;
specifier|final
name|String
name|coprocessor
init|=
literal|"org.apache.hadoop.hbase.coprocessor.SimpleRegionObserver"
decl_stmt|;
name|CoprocessorHost
argument_list|<
name|RegionCoprocessor
argument_list|,
name|CoprocessorEnvironment
argument_list|<
name|RegionCoprocessor
argument_list|>
argument_list|>
name|host
decl_stmt|;
name|host
operator|=
operator|new
name|CoprocessorHostForTest
argument_list|<>
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|int
name|overridePriority
init|=
name|Integer
operator|.
name|MAX_VALUE
operator|-
literal|1
decl_stmt|;
specifier|final
name|String
name|coprocessor_v3
init|=
name|SimpleRegionObserverV3
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|+
literal|"|"
operator|+
name|overridePriority
decl_stmt|;
comment|// Try and load a coprocessor three times
name|conf
operator|.
name|setStrings
argument_list|(
name|key
argument_list|,
name|coprocessor
argument_list|,
name|coprocessor
argument_list|,
name|coprocessor
argument_list|,
name|SimpleRegionObserverV2
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|coprocessor_v3
argument_list|)
expr_stmt|;
name|host
operator|.
name|loadSystemCoprocessors
argument_list|(
name|conf
argument_list|,
name|key
argument_list|)
expr_stmt|;
comment|// Three coprocessors(SimpleRegionObserver, SimpleRegionObserverV2,
comment|// SimpleRegionObserverV3) loaded
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|host
operator|.
name|coprocEnvironments
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// Check the priority value
name|CoprocessorEnvironment
argument_list|<
name|?
argument_list|>
name|simpleEnv
init|=
name|host
operator|.
name|findCoprocessorEnvironment
argument_list|(
name|SimpleRegionObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|CoprocessorEnvironment
argument_list|<
name|?
argument_list|>
name|simpleEnv_v2
init|=
name|host
operator|.
name|findCoprocessorEnvironment
argument_list|(
name|SimpleRegionObserverV2
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|CoprocessorEnvironment
argument_list|<
name|?
argument_list|>
name|simpleEnv_v3
init|=
name|host
operator|.
name|findCoprocessorEnvironment
argument_list|(
name|SimpleRegionObserverV3
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|simpleEnv
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|simpleEnv_v2
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|simpleEnv_v3
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Coprocessor
operator|.
name|PRIORITY_SYSTEM
argument_list|,
name|simpleEnv
operator|.
name|getPriority
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|Coprocessor
operator|.
name|PRIORITY_SYSTEM
operator|+
literal|1
argument_list|,
name|simpleEnv_v2
operator|.
name|getPriority
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|overridePriority
argument_list|,
name|simpleEnv_v3
operator|.
name|getPriority
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|SimpleRegionObserverV2
extends|extends
name|SimpleRegionObserver
block|{ }
specifier|public
specifier|static
class|class
name|SimpleRegionObserverV3
extends|extends
name|SimpleRegionObserver
block|{    }
specifier|private
specifier|static
class|class
name|CoprocessorHostForTest
parameter_list|<
name|E
extends|extends
name|Coprocessor
parameter_list|>
extends|extends
name|CoprocessorHost
argument_list|<
name|E
argument_list|,
name|CoprocessorEnvironment
argument_list|<
name|E
argument_list|>
argument_list|>
block|{
specifier|final
name|Configuration
name|cpHostConf
decl_stmt|;
specifier|public
name|CoprocessorHostForTest
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
operator|new
name|TestAbortable
argument_list|()
argument_list|)
expr_stmt|;
name|cpHostConf
operator|=
name|conf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|E
name|checkAndGetInstance
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|implClass
parameter_list|)
throws|throws
name|InstantiationException
throws|,
name|IllegalAccessException
block|{
try|try
block|{
return|return
operator|(
name|E
operator|)
name|implClass
operator|.
name|getDeclaredConstructor
argument_list|()
operator|.
name|newInstance
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
decl||
name|NoSuchMethodException
name|e
parameter_list|)
block|{
throw|throw
operator|(
name|InstantiationException
operator|)
operator|new
name|InstantiationException
argument_list|()
operator|.
name|initCause
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|CoprocessorEnvironment
argument_list|<
name|E
argument_list|>
name|createEnvironment
parameter_list|(
specifier|final
name|E
name|instance
parameter_list|,
specifier|final
name|int
name|priority
parameter_list|,
name|int
name|sequence
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
return|return
operator|new
name|BaseEnvironment
argument_list|<>
argument_list|(
name|instance
argument_list|,
name|priority
argument_list|,
literal|0
argument_list|,
name|cpHostConf
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

