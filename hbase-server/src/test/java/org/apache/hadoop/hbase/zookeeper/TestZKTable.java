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
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
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
name|BeforeClass
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
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestZKTable
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|TestZooKeeperNodeTracker
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTableStates
parameter_list|()
throws|throws
name|ZooKeeperConnectionException
throws|,
name|IOException
throws|,
name|KeeperException
block|{
specifier|final
name|String
name|name
init|=
literal|"testDisabled"
decl_stmt|;
name|Abortable
name|abortable
init|=
operator|new
name|Abortable
argument_list|()
block|{
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
name|LOG
operator|.
name|info
argument_list|(
name|why
argument_list|,
name|e
argument_list|)
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
literal|false
return|;
block|}
block|}
decl_stmt|;
name|ZooKeeperWatcher
name|zkw
init|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|name
argument_list|,
name|abortable
argument_list|,
literal|true
argument_list|)
decl_stmt|;
name|ZKTable
name|zkt
init|=
operator|new
name|ZKTable
argument_list|(
name|zkw
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|zkt
operator|.
name|isEnabledTable
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|zkt
operator|.
name|isDisablingTable
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|zkt
operator|.
name|isDisabledTable
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|zkt
operator|.
name|isEnablingTable
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|zkt
operator|.
name|isDisablingOrDisabledTable
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|zkt
operator|.
name|isDisabledOrEnablingTable
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|zkt
operator|.
name|isTablePresent
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|zkt
operator|.
name|setDisablingTable
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|zkt
operator|.
name|isDisablingTable
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|zkt
operator|.
name|isDisablingOrDisabledTable
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|zkt
operator|.
name|getDisabledTables
argument_list|()
operator|.
name|contains
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|zkt
operator|.
name|isTablePresent
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|zkt
operator|.
name|setDisabledTable
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|zkt
operator|.
name|isDisabledTable
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|zkt
operator|.
name|isDisablingOrDisabledTable
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|zkt
operator|.
name|isDisablingTable
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|zkt
operator|.
name|getDisabledTables
argument_list|()
operator|.
name|contains
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|zkt
operator|.
name|isTablePresent
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|zkt
operator|.
name|setEnablingTable
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|zkt
operator|.
name|isEnablingTable
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|zkt
operator|.
name|isDisabledOrEnablingTable
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|zkt
operator|.
name|isDisabledTable
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|zkt
operator|.
name|getDisabledTables
argument_list|()
operator|.
name|contains
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|zkt
operator|.
name|isTablePresent
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|zkt
operator|.
name|setEnabledTable
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|zkt
operator|.
name|isEnabledTable
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|zkt
operator|.
name|isEnablingTable
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|zkt
operator|.
name|isTablePresent
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|zkt
operator|.
name|setDeletedTable
argument_list|(
name|name
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|zkt
operator|.
name|isEnabledTable
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|zkt
operator|.
name|isDisablingTable
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|zkt
operator|.
name|isDisabledTable
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|zkt
operator|.
name|isEnablingTable
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|zkt
operator|.
name|isDisablingOrDisabledTable
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|zkt
operator|.
name|isDisabledOrEnablingTable
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|zkt
operator|.
name|isTablePresent
argument_list|(
name|name
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

