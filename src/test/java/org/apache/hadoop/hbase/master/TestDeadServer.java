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
name|master
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
name|assertFalse
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
name|junit
operator|.
name|Test
import|;
end_import

begin_class
specifier|public
class|class
name|TestDeadServer
block|{
annotation|@
name|Test
specifier|public
name|void
name|testIsDead
parameter_list|()
block|{
name|DeadServer
name|ds
init|=
operator|new
name|DeadServer
argument_list|(
literal|2
argument_list|)
decl_stmt|;
specifier|final
name|String
name|hostname123
init|=
literal|"127.0.0.1,123,3"
decl_stmt|;
name|assertFalse
argument_list|(
name|ds
operator|.
name|isDeadServer
argument_list|(
name|hostname123
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|ds
operator|.
name|isDeadServer
argument_list|(
name|hostname123
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|ds
operator|.
name|add
argument_list|(
name|hostname123
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ds
operator|.
name|isDeadServer
argument_list|(
name|hostname123
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|ds
operator|.
name|isDeadServer
argument_list|(
literal|"127.0.0.1:1"
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|ds
operator|.
name|isDeadServer
argument_list|(
literal|"127.0.0.1:1234"
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ds
operator|.
name|isDeadServer
argument_list|(
literal|"127.0.0.1:123"
argument_list|,
literal|true
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ds
operator|.
name|areDeadServersInProgress
argument_list|()
argument_list|)
expr_stmt|;
name|ds
operator|.
name|finish
argument_list|(
name|hostname123
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|ds
operator|.
name|areDeadServersInProgress
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|String
name|hostname1234
init|=
literal|"127.0.0.2,1234,4"
decl_stmt|;
name|ds
operator|.
name|add
argument_list|(
name|hostname1234
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ds
operator|.
name|isDeadServer
argument_list|(
name|hostname123
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ds
operator|.
name|isDeadServer
argument_list|(
name|hostname1234
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ds
operator|.
name|areDeadServersInProgress
argument_list|()
argument_list|)
expr_stmt|;
name|ds
operator|.
name|finish
argument_list|(
name|hostname1234
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|ds
operator|.
name|areDeadServersInProgress
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|String
name|hostname12345
init|=
literal|"127.0.0.2,12345,4"
decl_stmt|;
name|ds
operator|.
name|add
argument_list|(
name|hostname12345
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ds
operator|.
name|isDeadServer
argument_list|(
name|hostname1234
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ds
operator|.
name|isDeadServer
argument_list|(
name|hostname12345
argument_list|,
literal|false
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ds
operator|.
name|areDeadServersInProgress
argument_list|()
argument_list|)
expr_stmt|;
name|ds
operator|.
name|finish
argument_list|(
name|hostname12345
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|ds
operator|.
name|areDeadServersInProgress
argument_list|()
argument_list|)
expr_stmt|;
comment|// Already dead =       127.0.0.1,9090,112321
comment|// Coming back alive =  127.0.0.1,9090,223341
specifier|final
name|String
name|deadServer
init|=
literal|"127.0.0.1,9090,112321"
decl_stmt|;
name|assertFalse
argument_list|(
name|ds
operator|.
name|cleanPreviousInstance
argument_list|(
name|deadServer
argument_list|)
argument_list|)
expr_stmt|;
name|ds
operator|.
name|add
argument_list|(
name|deadServer
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|ds
operator|.
name|isDeadServer
argument_list|(
name|deadServer
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|String
name|deadServerHostComingAlive
init|=
literal|"127.0.0.1,9090,112321"
decl_stmt|;
name|assertTrue
argument_list|(
name|ds
operator|.
name|cleanPreviousInstance
argument_list|(
name|deadServerHostComingAlive
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|ds
operator|.
name|isDeadServer
argument_list|(
name|deadServer
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|ds
operator|.
name|cleanPreviousInstance
argument_list|(
name|deadServerHostComingAlive
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

