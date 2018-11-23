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
name|procedure
package|;
end_package

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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Procedures that operates on a specific Table (e.g. create, delete, snapshot, ...)  * must implement this interface to allow the system handle the lock/concurrency problems.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|TableProcedureInterface
block|{
comment|/**    * Used for acquire/release lock for namespace related operations, just a place holder as we do    * not have namespace table any more.    */
specifier|public
specifier|static
specifier|final
name|TableName
name|DUMMY_NAMESPACE_TABLE_NAME
init|=
name|TableName
operator|.
name|NAMESPACE_TABLE_NAME
decl_stmt|;
specifier|public
enum|enum
name|TableOperationType
block|{
name|CREATE
block|,
name|DELETE
block|,
name|DISABLE
block|,
name|EDIT
block|,
name|ENABLE
block|,
name|READ
block|,
name|REGION_EDIT
block|,
name|REGION_SPLIT
block|,
name|REGION_MERGE
block|,
name|REGION_ASSIGN
block|,
name|REGION_UNASSIGN
block|,
name|REGION_GC
block|,
name|MERGED_REGIONS_GC
comment|/* region operations */
block|}
comment|/**    * @return the name of the table the procedure is operating on    */
name|TableName
name|getTableName
parameter_list|()
function_decl|;
comment|/**    * Given an operation type we can take decisions about what to do with pending operations.    * e.g. if we get a delete and we have some table operation pending (e.g. add column)    * we can abort those operations.    * @return the operation type that the procedure is executing.    */
name|TableOperationType
name|getTableOperationType
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

