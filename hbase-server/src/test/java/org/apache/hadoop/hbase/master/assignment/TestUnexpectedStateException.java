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
operator|.
name|assignment
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
name|fail
import|;
end_import

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
name|Iterator
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
name|HBaseTestingUtility
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
name|client
operator|.
name|Admin
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
name|client
operator|.
name|RegionInfo
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
name|MediumTests
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
name|Bytes
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
name|Threads
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|gson
operator|.
name|JsonArray
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|gson
operator|.
name|JsonElement
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|gson
operator|.
name|JsonObject
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|gson
operator|.
name|JsonParser
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
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
name|BeforeClass
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
name|TestName
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Tests for HBASE-18408 "AM consumes CPU and fills up the logs really fast when there is no RS to  * assign". If an {@link org.apache.hadoop.hbase.exceptions.UnexpectedStateException}, we'd spin on  * the ProcedureExecutor consuming CPU and filling logs. Test new back-off facility.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestUnexpectedStateException
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
name|TestUnexpectedStateException
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
specifier|final
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestUnexpectedStateException
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
decl_stmt|;
specifier|private
name|TableName
name|tableName
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|REGIONS
init|=
literal|10
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|beforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|afterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|before
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|tableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|createMultiRegionTable
argument_list|(
name|this
operator|.
name|tableName
argument_list|,
name|FAMILY
argument_list|,
name|REGIONS
argument_list|)
expr_stmt|;
block|}
specifier|private
name|RegionInfo
name|pickArbitraryRegion
parameter_list|(
name|Admin
name|admin
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regions
init|=
name|admin
operator|.
name|getRegions
argument_list|(
name|this
operator|.
name|tableName
argument_list|)
decl_stmt|;
return|return
name|regions
operator|.
name|get
argument_list|(
literal|3
argument_list|)
return|;
block|}
comment|/**    * Manufacture a state that will throw UnexpectedStateException.    * Change an assigned region's 'state' to be OPENING. That'll mess up a subsequent unassign    * causing it to throw UnexpectedStateException. We can easily manufacture this infinite retry    * state in UnassignProcedure because it has no startTransition. AssignProcedure does where it    * squashes whatever the current region state is making it OFFLINE. That makes it harder to mess    * it up. Make do with UnassignProcedure for now.    */
annotation|@
name|Test
specifier|public
name|void
name|testUnableToAssign
parameter_list|()
throws|throws
name|Exception
block|{
try|try
init|(
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
init|)
block|{
comment|// Pick a random region from this tests' table to play with. Get its RegionStateNode.
comment|// Clone it because the original will be changed by the system. We need clone to fake out
comment|// a state.
specifier|final
name|RegionInfo
name|region
init|=
name|pickArbitraryRegion
argument_list|(
name|admin
argument_list|)
decl_stmt|;
name|AssignmentManager
name|am
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
name|RegionStates
operator|.
name|RegionStateNode
name|rsn
init|=
name|am
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionStateNode
argument_list|(
name|region
argument_list|)
decl_stmt|;
comment|// Now force region to be in OPENING state.
name|am
operator|.
name|markRegionAsOpening
argument_list|(
name|rsn
argument_list|)
expr_stmt|;
comment|// Now the 'region' is in an artificially bad state, try an unassign again.
comment|// Run unassign in a thread because it is blocking.
name|Runnable
name|unassign
init|=
parameter_list|()
lambda|->
block|{
try|try
block|{
name|admin
operator|.
name|unassign
argument_list|(
name|region
operator|.
name|getRegionName
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|fail
argument_list|(
literal|"Failed assign"
argument_list|)
expr_stmt|;
block|}
block|}
decl_stmt|;
name|Thread
name|t
init|=
operator|new
name|Thread
argument_list|(
name|unassign
argument_list|,
literal|"unassign"
argument_list|)
decl_stmt|;
name|t
operator|.
name|start
argument_list|()
expr_stmt|;
while|while
condition|(
operator|!
name|t
operator|.
name|isAlive
argument_list|()
condition|)
block|{
name|Threads
operator|.
name|sleep
argument_list|(
literal|100
argument_list|)
expr_stmt|;
block|}
name|Threads
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
comment|// Unassign should be running and failing. Look for incrementing timeout as evidence that
comment|// Unassign is stuck and doing backoff.
comment|// Now fix the condition we were waiting on so the unassign can complete.
name|JsonParser
name|parser
init|=
operator|new
name|JsonParser
argument_list|()
decl_stmt|;
name|long
name|oldTimeout
init|=
literal|0
decl_stmt|;
name|int
name|timeoutIncrements
init|=
literal|0
decl_stmt|;
while|while
condition|(
literal|true
condition|)
block|{
name|long
name|timeout
init|=
name|getUnassignTimeout
argument_list|(
name|parser
argument_list|,
name|admin
operator|.
name|getProcedures
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|timeout
operator|>
name|oldTimeout
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Timeout incremented, was {}, now is {}, increments={}"
argument_list|,
name|timeout
argument_list|,
name|oldTimeout
argument_list|,
name|timeoutIncrements
argument_list|)
expr_stmt|;
name|oldTimeout
operator|=
name|timeout
expr_stmt|;
name|timeoutIncrements
operator|++
expr_stmt|;
if|if
condition|(
name|timeoutIncrements
operator|>
literal|3
condition|)
block|{
comment|// If we incremented at least twice, break; the backoff is working.
break|break;
block|}
block|}
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
name|am
operator|.
name|markRegionAsOpened
argument_list|(
name|rsn
argument_list|)
expr_stmt|;
name|t
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * @param proceduresAsJSON This is String returned by admin.getProcedures call... an array of    *                         Procedures as JSON.    * @return The Procedure timeout value parsed from the Unassign Procedure.    * @Exception Thrown if we do not find UnassignProcedure or fail to parse timeout.    */
specifier|private
name|long
name|getUnassignTimeout
parameter_list|(
name|JsonParser
name|parser
parameter_list|,
name|String
name|proceduresAsJSON
parameter_list|)
throws|throws
name|Exception
block|{
name|JsonArray
name|array
init|=
name|parser
operator|.
name|parse
argument_list|(
name|proceduresAsJSON
argument_list|)
operator|.
name|getAsJsonArray
argument_list|()
decl_stmt|;
name|Iterator
argument_list|<
name|JsonElement
argument_list|>
name|iterator
init|=
name|array
operator|.
name|iterator
argument_list|()
decl_stmt|;
while|while
condition|(
name|iterator
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|JsonElement
name|element
init|=
name|iterator
operator|.
name|next
argument_list|()
decl_stmt|;
name|JsonObject
name|obj
init|=
name|element
operator|.
name|getAsJsonObject
argument_list|()
decl_stmt|;
name|String
name|className
init|=
name|obj
operator|.
name|get
argument_list|(
literal|"className"
argument_list|)
operator|.
name|getAsString
argument_list|()
decl_stmt|;
name|String
name|actualClassName
init|=
name|UnassignProcedure
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
if|if
condition|(
name|className
operator|.
name|equals
argument_list|(
name|actualClassName
argument_list|)
condition|)
block|{
return|return
name|obj
operator|.
name|get
argument_list|(
literal|"timeout"
argument_list|)
operator|.
name|getAsLong
argument_list|()
return|;
block|}
block|}
throw|throw
operator|new
name|Exception
argument_list|(
literal|"Failed to find UnassignProcedure or timeout in "
operator|+
name|proceduresAsJSON
argument_list|)
throw|;
block|}
block|}
end_class

end_unit

