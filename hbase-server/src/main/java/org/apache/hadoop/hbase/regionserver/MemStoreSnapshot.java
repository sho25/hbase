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
name|regionserver
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Holds details of the snapshot taken on a MemStore. Details include the snapshot's identifier,  * count of cells in it and total memory size occupied by all the cells, timestamp information of  * all the cells and a scanner to read all cells in it.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MemStoreSnapshot
block|{
specifier|private
specifier|final
name|long
name|id
decl_stmt|;
specifier|private
specifier|final
name|int
name|cellsCount
decl_stmt|;
specifier|private
specifier|final
name|long
name|size
decl_stmt|;
specifier|private
specifier|final
name|TimeRangeTracker
name|timeRangeTracker
decl_stmt|;
specifier|private
specifier|final
name|KeyValueScanner
name|scanner
decl_stmt|;
specifier|private
specifier|final
name|boolean
name|tagsPresent
decl_stmt|;
specifier|public
name|MemStoreSnapshot
parameter_list|(
name|long
name|id
parameter_list|,
name|int
name|cellsCount
parameter_list|,
name|long
name|size
parameter_list|,
name|TimeRangeTracker
name|timeRangeTracker
parameter_list|,
name|KeyValueScanner
name|scanner
parameter_list|,
name|boolean
name|tagsPresent
parameter_list|)
block|{
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|cellsCount
operator|=
name|cellsCount
expr_stmt|;
name|this
operator|.
name|size
operator|=
name|size
expr_stmt|;
name|this
operator|.
name|timeRangeTracker
operator|=
name|timeRangeTracker
expr_stmt|;
name|this
operator|.
name|scanner
operator|=
name|scanner
expr_stmt|;
name|this
operator|.
name|tagsPresent
operator|=
name|tagsPresent
expr_stmt|;
block|}
comment|/**    * @return snapshot's identifier.    */
specifier|public
name|long
name|getId
parameter_list|()
block|{
return|return
name|id
return|;
block|}
comment|/**    * @return Number of Cells in this snapshot.    */
specifier|public
name|int
name|getCellsCount
parameter_list|()
block|{
return|return
name|cellsCount
return|;
block|}
comment|/**    * @return Total memory size occupied by this snapshot.    */
specifier|public
name|long
name|getSize
parameter_list|()
block|{
return|return
name|size
return|;
block|}
comment|/**    * @return {@link TimeRangeTracker} for all the Cells in the snapshot.    */
specifier|public
name|TimeRangeTracker
name|getTimeRangeTracker
parameter_list|()
block|{
return|return
name|this
operator|.
name|timeRangeTracker
return|;
block|}
comment|/**    * @return {@link KeyValueScanner} for iterating over the snapshot    */
specifier|public
name|KeyValueScanner
name|getScanner
parameter_list|()
block|{
return|return
name|this
operator|.
name|scanner
return|;
block|}
comment|/**    * @return true if tags are present in this snapshot    */
specifier|public
name|boolean
name|isTagsPresent
parameter_list|()
block|{
return|return
name|this
operator|.
name|tagsPresent
return|;
block|}
block|}
end_class

end_unit

