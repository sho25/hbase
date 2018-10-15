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
name|client
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
name|io
operator|.
name|IOException
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
name|RestoreSnapshotFromClientGetCompactionStateTestBase
extends|extends
name|RestoreSnapshotFromClientTestBase
block|{
annotation|@
name|Test
specifier|public
name|void
name|testGetCompactionStateAfterRestoringSnapshot
parameter_list|()
throws|throws
name|IOException
throws|,
name|InterruptedException
block|{
comment|// Take a snapshot
name|admin
operator|.
name|snapshot
argument_list|(
name|snapshotName1
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
comment|// Restore the snapshot
name|admin
operator|.
name|disableTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|restoreSnapshot
argument_list|(
name|snapshotName1
argument_list|)
expr_stmt|;
comment|// Get the compaction state of the restored table
name|CompactionState
name|compactionState
init|=
name|admin
operator|.
name|getCompactionState
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
comment|// The compactionState should be NONE because the table is disabled
name|assertEquals
argument_list|(
name|CompactionState
operator|.
name|NONE
argument_list|,
name|compactionState
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

