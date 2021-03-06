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
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|ServerName
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
name|MasterTests
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
name|EnvironmentEdgeManager
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
name|ManualEnvironmentEdge
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
name|Pair
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
name|Before
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
name|MasterTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
comment|// Plays with the ManualEnvironmentEdge
specifier|public
class|class
name|TestClusterStatusPublisher
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
name|TestClusterStatusPublisher
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|ManualEnvironmentEdge
name|mee
init|=
operator|new
name|ManualEnvironmentEdge
argument_list|()
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|before
parameter_list|()
block|{
name|mee
operator|.
name|setValue
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|EnvironmentEdgeManager
operator|.
name|injectEdge
argument_list|(
name|mee
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEmpty
parameter_list|()
block|{
name|ClusterStatusPublisher
name|csp
init|=
operator|new
name|ClusterStatusPublisher
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|Pair
argument_list|<
name|ServerName
argument_list|,
name|Long
argument_list|>
argument_list|>
name|getDeadServers
parameter_list|(
name|long
name|since
parameter_list|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<>
argument_list|()
return|;
block|}
block|}
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|csp
operator|.
name|generateDeadServersListToSend
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMaxSend
parameter_list|()
block|{
name|ClusterStatusPublisher
name|csp
init|=
operator|new
name|ClusterStatusPublisher
argument_list|()
block|{
annotation|@
name|SuppressWarnings
argument_list|(
literal|"MissingDefault"
argument_list|)
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|Pair
argument_list|<
name|ServerName
argument_list|,
name|Long
argument_list|>
argument_list|>
name|getDeadServers
parameter_list|(
name|long
name|since
parameter_list|)
block|{
name|List
argument_list|<
name|Pair
argument_list|<
name|ServerName
argument_list|,
name|Long
argument_list|>
argument_list|>
name|res
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
switch|switch
condition|(
operator|(
name|int
operator|)
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
condition|)
block|{
case|case
literal|2
case|:
name|res
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|<>
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"hn"
argument_list|,
literal|10
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|1L
argument_list|)
argument_list|)
expr_stmt|;
break|break;
case|case
literal|1000
case|:
break|break;
block|}
return|return
name|res
return|;
block|}
block|}
decl_stmt|;
name|mee
operator|.
name|setValue
argument_list|(
literal|2
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|ClusterStatusPublisher
operator|.
name|NB_SEND
condition|;
name|i
operator|++
control|)
block|{
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|"i="
operator|+
name|i
argument_list|,
literal|1
argument_list|,
name|csp
operator|.
name|generateDeadServersListToSend
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|mee
operator|.
name|setValue
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|csp
operator|.
name|generateDeadServersListToSend
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOrder
parameter_list|()
block|{
name|ClusterStatusPublisher
name|csp
init|=
operator|new
name|ClusterStatusPublisher
argument_list|()
block|{
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|Pair
argument_list|<
name|ServerName
argument_list|,
name|Long
argument_list|>
argument_list|>
name|getDeadServers
parameter_list|(
name|long
name|since
parameter_list|)
block|{
name|List
argument_list|<
name|Pair
argument_list|<
name|ServerName
argument_list|,
name|Long
argument_list|>
argument_list|>
name|res
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
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
literal|25
condition|;
name|i
operator|++
control|)
block|{
name|res
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|<>
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"hn"
operator|+
name|i
argument_list|,
literal|10
argument_list|,
literal|10
argument_list|)
argument_list|,
literal|20L
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|res
return|;
block|}
block|}
decl_stmt|;
name|mee
operator|.
name|setValue
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|allSNS
init|=
name|csp
operator|.
name|generateDeadServersListToSend
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|ClusterStatusPublisher
operator|.
name|MAX_SERVER_PER_MESSAGE
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|allSNS
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|nextMes
init|=
name|csp
operator|.
name|generateDeadServersListToSend
argument_list|()
decl_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|nextMes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ServerName
name|sn
range|:
name|nextMes
control|)
block|{
if|if
condition|(
operator|!
name|allSNS
operator|.
name|contains
argument_list|(
name|sn
argument_list|)
condition|)
block|{
name|allSNS
operator|.
name|add
argument_list|(
name|sn
argument_list|)
expr_stmt|;
block|}
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|20
argument_list|,
name|allSNS
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|nextMes
operator|=
name|csp
operator|.
name|generateDeadServersListToSend
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|nextMes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ServerName
name|sn
range|:
name|nextMes
control|)
block|{
if|if
condition|(
operator|!
name|allSNS
operator|.
name|contains
argument_list|(
name|sn
argument_list|)
condition|)
block|{
name|allSNS
operator|.
name|add
argument_list|(
name|sn
argument_list|)
expr_stmt|;
block|}
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|25
argument_list|,
name|allSNS
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|nextMes
operator|=
name|csp
operator|.
name|generateDeadServersListToSend
argument_list|()
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|10
argument_list|,
name|nextMes
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|ServerName
name|sn
range|:
name|nextMes
control|)
block|{
if|if
condition|(
operator|!
name|allSNS
operator|.
name|contains
argument_list|(
name|sn
argument_list|)
condition|)
block|{
name|allSNS
operator|.
name|add
argument_list|(
name|sn
argument_list|)
expr_stmt|;
block|}
block|}
name|Assert
operator|.
name|assertEquals
argument_list|(
literal|25
argument_list|,
name|allSNS
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

