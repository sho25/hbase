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
name|mob
operator|.
name|filecompactions
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * The compaction request for mob files.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|MobFileCompactionRequest
block|{
specifier|protected
name|long
name|selectionTime
decl_stmt|;
specifier|protected
name|CompactionType
name|type
init|=
name|CompactionType
operator|.
name|PART_FILES
decl_stmt|;
specifier|public
name|void
name|setCompactionType
parameter_list|(
name|CompactionType
name|type
parameter_list|)
block|{
name|this
operator|.
name|type
operator|=
name|type
expr_stmt|;
block|}
comment|/**    * Gets the selection time.    * @return The selection time.    */
specifier|public
name|long
name|getSelectionTime
parameter_list|()
block|{
return|return
name|this
operator|.
name|selectionTime
return|;
block|}
comment|/**    * Gets the compaction type.    * @return The compaction type.    */
specifier|public
name|CompactionType
name|getCompactionType
parameter_list|()
block|{
return|return
name|type
return|;
block|}
specifier|protected
enum|enum
name|CompactionType
block|{
comment|/**      * Part of mob files are selected.      */
name|PART_FILES
block|,
comment|/**      * All of mob files are selected.      */
name|ALL_FILES
block|;   }
block|}
end_class

end_unit

