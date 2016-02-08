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
name|regionserver
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|lang
operator|.
name|NotImplementedException
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
name|Cell
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * ImmutableSegment is an abstract class that extends the API supported by a {@link Segment},  * and is not needed for a {@link MutableSegment}. Specifically, the method  * {@link ImmutableSegment#getKeyValueScanner()} builds a special scanner for the  * {@link MemStoreSnapshot} object.  * In addition, this class overrides methods that are not likely to be supported by an immutable  * segment, e.g. {@link Segment#rollback(Cell)} and {@link Segment#getCellSet()}, which  * can be very inefficient.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|ImmutableSegment
extends|extends
name|Segment
block|{
specifier|public
name|ImmutableSegment
parameter_list|(
name|Segment
name|segment
parameter_list|)
block|{
name|super
argument_list|(
name|segment
argument_list|)
expr_stmt|;
block|}
comment|/**    * Removes the given cell from this segment.    * By default immutable store segment can not rollback    * It may be invoked by tests in specific cases where it is known to be supported {@link    * ImmutableSegmentAdapter}    */
annotation|@
name|Override
specifier|public
name|long
name|rollback
parameter_list|(
name|Cell
name|cell
parameter_list|)
block|{
return|return
literal|0
return|;
block|}
comment|/**    * Returns a set of all the cells in the segment.    * The implementation of this method might be very inefficient for some immutable segments    * that do not maintain a cell set. Therefore by default this method is not supported.    * It may be invoked by tests in specific cases where it is known to be supported {@link    * ImmutableSegmentAdapter}    */
annotation|@
name|Override
specifier|public
name|CellSet
name|getCellSet
parameter_list|()
block|{
throw|throw
operator|new
name|NotImplementedException
argument_list|(
literal|"Immutable Segment does not support this operation by "
operator|+
literal|"default"
argument_list|)
throw|;
block|}
comment|/**    * Builds a special scanner for the MemStoreSnapshot object that may be different than the    * general segment scanner.    * @return a special scanner for the MemStoreSnapshot object    */
specifier|public
specifier|abstract
name|KeyValueScanner
name|getKeyValueScanner
parameter_list|()
function_decl|;
block|}
end_class

end_unit

