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
name|hadoop
operator|.
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
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
name|CollectionBackedScanner
import|;
end_import

begin_comment
comment|/**  * ImmutableSegment is an abstract class that extends the API supported by a {@link Segment},  * and is not needed for a {@link MutableSegment}. Specifically, the method  * {@link ImmutableSegment#getKeyValueScanner()} builds a special scanner for the  * {@link MemStoreSnapshot} object.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ImmutableSegment
extends|extends
name|Segment
block|{
specifier|protected
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
comment|/**    * Builds a special scanner for the MemStoreSnapshot object that is different than the    * general segment scanner.    * @return a special scanner for the MemStoreSnapshot object    */
specifier|public
name|KeyValueScanner
name|getKeyValueScanner
parameter_list|()
block|{
return|return
operator|new
name|CollectionBackedScanner
argument_list|(
name|getCellSet
argument_list|()
argument_list|,
name|getComparator
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

