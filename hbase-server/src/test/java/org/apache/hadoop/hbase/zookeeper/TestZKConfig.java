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
name|zookeeper
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Properties
import|;
end_import

begin_import
import|import
name|junit
operator|.
name|framework
operator|.
name|Assert
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
name|HConstants
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
name|TestZKConfig
block|{
annotation|@
name|Test
specifier|public
name|void
name|testZKConfigLoading
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Test depends on test resource 'zoo.cfg' at src/test/resources/zoo.cfg
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
comment|// Test that by default we do not pick up any property from the zoo.cfg
comment|// since that feature is to be deprecated and removed. So we should read only
comment|// from the config instance (i.e. via hbase-default.xml and hbase-site.xml)
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_CLIENT_PORT
argument_list|,
literal|2181
argument_list|)
expr_stmt|;
name|Properties
name|props
init|=
name|ZKConfig
operator|.
name|makeZKProps
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Property client port should have been default from the HBase config"
argument_list|,
literal|"2181"
argument_list|,
name|props
operator|.
name|getProperty
argument_list|(
literal|"clientPort"
argument_list|)
argument_list|)
expr_stmt|;
comment|// Test deprecated zoo.cfg read support by explicitly enabling it and
comment|// thereby relying on our test resource zoo.cfg to be read.
comment|// We may remove this test after a higher release (i.e. post-deprecation).
name|conf
operator|.
name|setBoolean
argument_list|(
name|HConstants
operator|.
name|HBASE_CONFIG_READ_ZOOKEEPER_CONFIG
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|props
operator|=
name|ZKConfig
operator|.
name|makeZKProps
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"Property client port should have been from zoo.cfg"
argument_list|,
literal|"9999"
argument_list|,
name|props
operator|.
name|getProperty
argument_list|(
literal|"clientPort"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

