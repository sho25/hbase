begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|filter
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|java
operator|.
name|util
operator|.
name|SortedMap
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
name|io
operator|.
name|Cell
import|;
end_import

begin_comment
comment|/**  * WhileMatchRowFilter is a wrapper filter that filters everything after the   * first filtered row.  Once the nested filter returns true for either of it's   * filter(..) methods or filterNotNull(SortedMap<Text, byte[]>), this wrapper's   * filterAllRemaining() will return true.  All filtering methods will   * thereafter defer to the result of filterAllRemaining().  */
end_comment

begin_class
specifier|public
class|class
name|WhileMatchRowFilter
implements|implements
name|RowFilterInterface
block|{
specifier|private
name|boolean
name|filterAllRemaining
init|=
literal|false
decl_stmt|;
specifier|private
name|RowFilterInterface
name|filter
decl_stmt|;
comment|/**    * Default constructor, filters nothing. Required though for RPC    * deserialization.    */
specifier|public
name|WhileMatchRowFilter
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Constructor    * @param filter    */
specifier|public
name|WhileMatchRowFilter
parameter_list|(
name|RowFilterInterface
name|filter
parameter_list|)
block|{
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
block|}
comment|/**    * Returns the internal filter being wrapped    *     * @return the internal filter    */
specifier|public
name|RowFilterInterface
name|getInternalFilter
parameter_list|()
block|{
return|return
name|this
operator|.
name|filter
return|;
block|}
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|this
operator|.
name|filterAllRemaining
operator|=
literal|false
expr_stmt|;
name|this
operator|.
name|filter
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
specifier|public
name|boolean
name|processAlways
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
comment|/**    * Returns true once the nested filter has filtered out a row (returned true     * on a call to one of it's filtering methods).  Until then it returns false.    *     * @return true/false whether the nested filter has returned true on a filter     * call.    */
specifier|public
name|boolean
name|filterAllRemaining
parameter_list|()
block|{
return|return
name|this
operator|.
name|filterAllRemaining
operator|||
name|this
operator|.
name|filter
operator|.
name|filterAllRemaining
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
specifier|final
name|byte
index|[]
name|rowKey
parameter_list|)
block|{
name|changeFAR
argument_list|(
name|this
operator|.
name|filter
operator|.
name|filterRowKey
argument_list|(
name|rowKey
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|filterAllRemaining
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|filterColumn
parameter_list|(
specifier|final
name|byte
index|[]
name|rowKey
parameter_list|,
specifier|final
name|byte
index|[]
name|colKey
parameter_list|,
specifier|final
name|byte
index|[]
name|data
parameter_list|)
block|{
name|changeFAR
argument_list|(
name|this
operator|.
name|filter
operator|.
name|filterColumn
argument_list|(
name|rowKey
argument_list|,
name|colKey
argument_list|,
name|data
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|filterAllRemaining
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|filterRow
parameter_list|(
specifier|final
name|SortedMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
name|columns
parameter_list|)
block|{
name|changeFAR
argument_list|(
name|this
operator|.
name|filter
operator|.
name|filterRow
argument_list|(
name|columns
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|filterAllRemaining
argument_list|()
return|;
block|}
comment|/**    * Change filterAllRemaining from false to true if value is true, otherwise     * leave as is.    *     * @param value    */
specifier|private
name|void
name|changeFAR
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|this
operator|.
name|filterAllRemaining
operator|=
name|this
operator|.
name|filterAllRemaining
operator|||
name|value
expr_stmt|;
block|}
specifier|public
name|void
name|rowProcessed
parameter_list|(
name|boolean
name|filtered
parameter_list|,
name|byte
index|[]
name|rowKey
parameter_list|)
block|{
name|this
operator|.
name|filter
operator|.
name|rowProcessed
argument_list|(
name|filtered
argument_list|,
name|rowKey
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|validate
parameter_list|(
specifier|final
name|byte
index|[]
index|[]
name|columns
parameter_list|)
block|{
name|this
operator|.
name|filter
operator|.
name|validate
argument_list|(
name|columns
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|readFields
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|className
init|=
name|in
operator|.
name|readUTF
argument_list|()
decl_stmt|;
try|try
block|{
name|this
operator|.
name|filter
operator|=
call|(
name|RowFilterInterface
call|)
argument_list|(
name|Class
operator|.
name|forName
argument_list|(
name|className
argument_list|)
operator|.
name|newInstance
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|filter
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InstantiationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to deserialize WhileMatchRowFilter."
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to deserialize WhileMatchRowFilter."
argument_list|,
name|e
argument_list|)
throw|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Failed to deserialize WhileMatchRowFilter."
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|public
name|void
name|write
parameter_list|(
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|out
operator|.
name|writeUTF
argument_list|(
name|this
operator|.
name|filter
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|filter
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

