begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|assertTrue
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
name|*
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
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestZooKeeperMainServerArg
block|{
specifier|private
specifier|final
name|ZooKeeperMainServerArg
name|parser
init|=
operator|new
name|ZooKeeperMainServerArg
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
block|{
name|Configuration
name|c
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"localhost:"
operator|+
name|c
operator|.
name|get
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_CLIENT_PORT
argument_list|)
argument_list|,
name|parser
operator|.
name|parse
argument_list|(
name|c
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|String
name|port
init|=
literal|"1234"
decl_stmt|;
name|c
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|ZOOKEEPER_CLIENT_PORT
argument_list|,
name|port
argument_list|)
expr_stmt|;
name|c
operator|.
name|set
argument_list|(
literal|"hbase.zookeeper.quorum"
argument_list|,
literal|"example.com"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"example.com:"
operator|+
name|port
argument_list|,
name|parser
operator|.
name|parse
argument_list|(
name|c
argument_list|)
argument_list|)
expr_stmt|;
name|c
operator|.
name|set
argument_list|(
literal|"hbase.zookeeper.quorum"
argument_list|,
literal|"example1.com,example2.com,example3.com"
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|port
argument_list|,
name|parser
operator|.
name|parse
argument_list|(
name|c
argument_list|)
operator|.
name|matches
argument_list|(
literal|"example[1-3]\\.com:"
operator|+
name|port
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

