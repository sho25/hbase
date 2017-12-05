begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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
name|atomic
operator|.
name|AtomicBoolean
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|*
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
name|*
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
name|HRegionInfo
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
name|HTableDescriptor
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
name|TableName
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
name|master
operator|.
name|MasterCoprocessorHost
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
name|master
operator|.
name|MasterServices
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
name|regionserver
operator|.
name|HRegion
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
name|regionserver
operator|.
name|RegionCoprocessorHost
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
name|regionserver
operator|.
name|RegionServerCoprocessorHost
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
name|regionserver
operator|.
name|RegionServerServices
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
name|CoprocessorTests
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
name|Rule
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|rules
operator|.
name|ExpectedException
import|;
end_import

begin_comment
comment|/**  * Tests for global coprocessor loading configuration  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|CoprocessorTests
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
name|TestCoprocessorConfiguration
block|{
annotation|@
name|Rule
specifier|public
name|ExpectedException
name|thrown
init|=
name|ExpectedException
operator|.
name|none
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Configuration
name|CONF
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
static|static
block|{
name|CONF
operator|.
name|setStrings
argument_list|(
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|SystemCoprocessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|CONF
operator|.
name|setStrings
argument_list|(
name|CoprocessorHost
operator|.
name|REGIONSERVER_COPROCESSOR_CONF_KEY
argument_list|,
name|SystemCoprocessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|CONF
operator|.
name|setStrings
argument_list|(
name|CoprocessorHost
operator|.
name|REGION_COPROCESSOR_CONF_KEY
argument_list|,
name|SystemCoprocessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
name|TableName
name|TABLENAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestCoprocessorConfiguration"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HRegionInfo
name|REGIONINFO
init|=
operator|new
name|HRegionInfo
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HTableDescriptor
name|TABLEDESC
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TABLENAME
argument_list|)
decl_stmt|;
static|static
block|{
try|try
block|{
name|TABLEDESC
operator|.
name|addCoprocessor
argument_list|(
name|TableCoprocessor
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
comment|// use atomic types in case coprocessor loading is ever multithreaded, also
comment|// so we can mutate them even though they are declared final here
specifier|private
specifier|static
specifier|final
name|AtomicBoolean
name|systemCoprocessorLoaded
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|AtomicBoolean
name|tableCoprocessorLoaded
init|=
operator|new
name|AtomicBoolean
argument_list|()
decl_stmt|;
specifier|public
specifier|static
class|class
name|SystemCoprocessor
implements|implements
name|MasterCoprocessor
implements|,
name|RegionCoprocessor
implements|,
name|RegionServerCoprocessor
block|{
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|systemCoprocessorLoaded
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{ }
block|}
specifier|public
specifier|static
class|class
name|TableCoprocessor
implements|implements
name|RegionCoprocessor
block|{
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
name|tableCoprocessorLoaded
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{ }
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionCoprocessorHostDefaults
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|CONF
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|mock
argument_list|(
name|HRegion
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|REGIONINFO
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|region
operator|.
name|getTableDescriptor
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TABLEDESC
argument_list|)
expr_stmt|;
name|RegionServerServices
name|rsServices
init|=
name|mock
argument_list|(
name|RegionServerServices
operator|.
name|class
argument_list|)
decl_stmt|;
name|systemCoprocessorLoaded
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|tableCoprocessorLoaded
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
operator|new
name|RegionCoprocessorHost
argument_list|(
name|region
argument_list|,
name|rsServices
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"System coprocessors loading default was not honored"
argument_list|,
name|systemCoprocessorLoaded
operator|.
name|get
argument_list|()
argument_list|,
name|CoprocessorHost
operator|.
name|DEFAULT_COPROCESSORS_ENABLED
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"Table coprocessors loading default was not honored"
argument_list|,
name|tableCoprocessorLoaded
operator|.
name|get
argument_list|()
argument_list|,
name|CoprocessorHost
operator|.
name|DEFAULT_COPROCESSORS_ENABLED
operator|&&
name|CoprocessorHost
operator|.
name|DEFAULT_USER_COPROCESSORS_ENABLED
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionServerCoprocessorHostDefaults
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|CONF
argument_list|)
decl_stmt|;
name|RegionServerServices
name|rsServices
init|=
name|mock
argument_list|(
name|RegionServerServices
operator|.
name|class
argument_list|)
decl_stmt|;
name|systemCoprocessorLoaded
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
operator|new
name|RegionServerCoprocessorHost
argument_list|(
name|rsServices
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"System coprocessors loading default was not honored"
argument_list|,
name|systemCoprocessorLoaded
operator|.
name|get
argument_list|()
argument_list|,
name|CoprocessorHost
operator|.
name|DEFAULT_COPROCESSORS_ENABLED
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMasterCoprocessorHostDefaults
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|CONF
argument_list|)
decl_stmt|;
name|MasterServices
name|masterServices
init|=
name|mock
argument_list|(
name|MasterServices
operator|.
name|class
argument_list|)
decl_stmt|;
name|systemCoprocessorLoaded
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
operator|new
name|MasterCoprocessorHost
argument_list|(
name|masterServices
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"System coprocessors loading default was not honored"
argument_list|,
name|systemCoprocessorLoaded
operator|.
name|get
argument_list|()
argument_list|,
name|CoprocessorHost
operator|.
name|DEFAULT_COPROCESSORS_ENABLED
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionCoprocessorHostAllDisabled
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|CONF
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|CoprocessorHost
operator|.
name|COPROCESSORS_ENABLED_CONF_KEY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|HRegion
name|region
init|=
name|mock
argument_list|(
name|HRegion
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|REGIONINFO
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|region
operator|.
name|getTableDescriptor
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TABLEDESC
argument_list|)
expr_stmt|;
name|RegionServerServices
name|rsServices
init|=
name|mock
argument_list|(
name|RegionServerServices
operator|.
name|class
argument_list|)
decl_stmt|;
name|systemCoprocessorLoaded
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|tableCoprocessorLoaded
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
operator|new
name|RegionCoprocessorHost
argument_list|(
name|region
argument_list|,
name|rsServices
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"System coprocessors should not have been loaded"
argument_list|,
name|systemCoprocessorLoaded
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Table coprocessors should not have been loaded"
argument_list|,
name|tableCoprocessorLoaded
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionCoprocessorHostTableLoadingDisabled
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|CONF
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|CoprocessorHost
operator|.
name|COPROCESSORS_ENABLED_CONF_KEY
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// if defaults change
name|conf
operator|.
name|setBoolean
argument_list|(
name|CoprocessorHost
operator|.
name|USER_COPROCESSORS_ENABLED_CONF_KEY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|HRegion
name|region
init|=
name|mock
argument_list|(
name|HRegion
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|REGIONINFO
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|region
operator|.
name|getTableDescriptor
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TABLEDESC
argument_list|)
expr_stmt|;
name|RegionServerServices
name|rsServices
init|=
name|mock
argument_list|(
name|RegionServerServices
operator|.
name|class
argument_list|)
decl_stmt|;
name|systemCoprocessorLoaded
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|tableCoprocessorLoaded
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
operator|new
name|RegionCoprocessorHost
argument_list|(
name|region
argument_list|,
name|rsServices
argument_list|,
name|conf
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"System coprocessors should have been loaded"
argument_list|,
name|systemCoprocessorLoaded
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Table coprocessors should not have been loaded"
argument_list|,
name|tableCoprocessorLoaded
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Rough test that Coprocessor Environment is Read-Only.    * Just check a random CP and see that it returns a read-only config.    */
annotation|@
name|Test
specifier|public
name|void
name|testReadOnlyConfiguration
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|CONF
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|mock
argument_list|(
name|HRegion
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|REGIONINFO
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|region
operator|.
name|getTableDescriptor
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TABLEDESC
argument_list|)
expr_stmt|;
name|RegionServerServices
name|rsServices
init|=
name|mock
argument_list|(
name|RegionServerServices
operator|.
name|class
argument_list|)
decl_stmt|;
name|RegionCoprocessorHost
name|rcp
init|=
operator|new
name|RegionCoprocessorHost
argument_list|(
name|region
argument_list|,
name|rsServices
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|boolean
name|found
init|=
literal|false
decl_stmt|;
for|for
control|(
name|String
name|cpStr
range|:
name|rcp
operator|.
name|getCoprocessors
argument_list|()
control|)
block|{
name|CoprocessorEnvironment
name|cpenv
init|=
name|rcp
operator|.
name|findCoprocessorEnvironment
argument_list|(
name|cpStr
argument_list|)
decl_stmt|;
if|if
condition|(
name|cpenv
operator|!=
literal|null
condition|)
block|{
name|found
operator|=
literal|true
expr_stmt|;
block|}
name|Configuration
name|c
init|=
name|cpenv
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|thrown
operator|.
name|expect
argument_list|(
name|UnsupportedOperationException
operator|.
name|class
argument_list|)
expr_stmt|;
name|c
operator|.
name|set
argument_list|(
literal|"one.two.three"
argument_list|,
literal|"four.five.six"
argument_list|)
expr_stmt|;
block|}
name|assertTrue
argument_list|(
literal|"Should be at least one CP found"
argument_list|,
name|found
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

