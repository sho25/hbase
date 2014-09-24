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

begin_comment
comment|/**  * Simple wrapper for a byte buffer and a counter.  Does not copy.  *<p>  * NOT thread-safe because it is not used in a multi-threaded context, yet.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ColumnCount
block|{
specifier|private
specifier|final
name|byte
index|[]
name|bytes
decl_stmt|;
specifier|private
specifier|final
name|int
name|offset
decl_stmt|;
specifier|private
specifier|final
name|int
name|length
decl_stmt|;
specifier|private
name|int
name|count
decl_stmt|;
comment|/**    * Constructor    * @param column the qualifier to count the versions for    */
specifier|public
name|ColumnCount
parameter_list|(
name|byte
index|[]
name|column
parameter_list|)
block|{
name|this
argument_list|(
name|column
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor    * @param column the qualifier to count the versions for    * @param count initial count    */
specifier|public
name|ColumnCount
parameter_list|(
name|byte
index|[]
name|column
parameter_list|,
name|int
name|count
parameter_list|)
block|{
name|this
argument_list|(
name|column
argument_list|,
literal|0
argument_list|,
name|column
operator|.
name|length
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constuctor    * @param column the qualifier to count the versions for    * @param offset in the passed buffer where to start the qualifier from    * @param length of the qualifier    * @param count initial count    */
specifier|public
name|ColumnCount
parameter_list|(
name|byte
index|[]
name|column
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|int
name|count
parameter_list|)
block|{
name|this
operator|.
name|bytes
operator|=
name|column
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
name|this
operator|.
name|count
operator|=
name|count
expr_stmt|;
block|}
comment|/**    * @return the buffer    */
specifier|public
name|byte
index|[]
name|getBuffer
parameter_list|()
block|{
return|return
name|this
operator|.
name|bytes
return|;
block|}
comment|/**    * @return the offset    */
specifier|public
name|int
name|getOffset
parameter_list|()
block|{
return|return
name|this
operator|.
name|offset
return|;
block|}
comment|/**    * @return the length    */
specifier|public
name|int
name|getLength
parameter_list|()
block|{
return|return
name|this
operator|.
name|length
return|;
block|}
comment|/**    * Decrement the current version count    * @return current count    */
specifier|public
name|int
name|decrement
parameter_list|()
block|{
return|return
operator|--
name|count
return|;
block|}
comment|/**    * Increment the current version count    * @return current count    */
specifier|public
name|int
name|increment
parameter_list|()
block|{
return|return
operator|++
name|count
return|;
block|}
comment|/**    * Set the current count to a new count    * @param count new count to set    */
specifier|public
name|void
name|setCount
parameter_list|(
name|int
name|count
parameter_list|)
block|{
name|this
operator|.
name|count
operator|=
name|count
expr_stmt|;
block|}
block|}
end_class

end_unit

