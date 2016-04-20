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
operator|.
name|compactions
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
comment|/**  * Base class for compaction window implementation.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|CompactionWindow
block|{
comment|/**    * Compares the window to a timestamp.    * @param timestamp the timestamp to compare.    * @return a negative integer, zero, or a positive integer as the window lies before, covering, or    *         after than the timestamp.    */
specifier|public
specifier|abstract
name|int
name|compareToTimestamp
parameter_list|(
name|long
name|timestamp
parameter_list|)
function_decl|;
comment|/**    * Move to the new window of the same tier or of the next tier, which represents an earlier time    * span.    * @return The next earlier window    */
specifier|public
specifier|abstract
name|CompactionWindow
name|nextEarlierWindow
parameter_list|()
function_decl|;
comment|/**    * Inclusive lower bound    */
specifier|public
specifier|abstract
name|long
name|startMillis
parameter_list|()
function_decl|;
comment|/**    * Exclusive upper bound    */
specifier|public
specifier|abstract
name|long
name|endMillis
parameter_list|()
function_decl|;
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"["
operator|+
name|startMillis
argument_list|()
operator|+
literal|", "
operator|+
name|endMillis
argument_list|()
operator|+
literal|")"
return|;
block|}
block|}
end_class

end_unit

