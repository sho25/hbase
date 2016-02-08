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
name|java
operator|.
name|util
operator|.
name|SortedSet
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
name|CellComparator
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
comment|/**  * An abstraction of a mutable segment in memstore, specifically the active segment.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|MutableSegment
extends|extends
name|Segment
block|{
specifier|protected
name|MutableSegment
parameter_list|(
name|MemStoreLAB
name|memStoreLAB
parameter_list|,
name|long
name|size
parameter_list|)
block|{
name|super
argument_list|(
name|memStoreLAB
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
comment|/**    * Returns a subset of the segment cell set, which starts with the given cell    * @param firstCell a cell in the segment    * @return a subset of the segment cell set, which starts with the given cell    */
specifier|public
specifier|abstract
name|SortedSet
argument_list|<
name|Cell
argument_list|>
name|tailSet
parameter_list|(
name|Cell
name|firstCell
parameter_list|)
function_decl|;
comment|/**    * Returns the Cell comparator used by this segment    * @return the Cell comparator used by this segment    */
specifier|public
specifier|abstract
name|CellComparator
name|getComparator
parameter_list|()
function_decl|;
comment|//methods for test
comment|/**    * Returns the first cell in the segment    * @return the first cell in the segment    */
specifier|abstract
name|Cell
name|first
parameter_list|()
function_decl|;
block|}
end_class

end_unit

