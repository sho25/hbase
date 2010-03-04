begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2009 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|KeyValue
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
name|io
operator|.
name|DataInput
import|;
end_import

begin_comment
comment|/**  * A wrapper filter that filters an entire row if any of the KeyValue checks do   * not pass.  *<p>  * For example, if all columns in a row represent weights of different things,  * with the values being the actual weights, and we want to filter out the  * entire row if any of its weights are zero.  In this case, we want to prevent  * rows from being emitted if a single key is filtered.  Combine this filter  * with a {@link ValueFilter}:  *<p>  *<pre>  * scan.setFilter(new SkipFilter(new ValueFilter(CompareOp.EQUAL,  *     new BinaryComparator(Bytes.toBytes(0))));  *</code>  * Any row which contained a column whose value was 0 will be filtered out.  * Without this filter, the other non-zero valued columns in the row would still   * be emitted.  */
end_comment

begin_class
specifier|public
class|class
name|SkipFilter
implements|implements
name|Filter
block|{
specifier|private
name|boolean
name|filterRow
init|=
literal|false
decl_stmt|;
specifier|private
name|Filter
name|filter
decl_stmt|;
specifier|public
name|SkipFilter
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
specifier|public
name|SkipFilter
parameter_list|(
name|Filter
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
specifier|public
name|Filter
name|getFilter
parameter_list|()
block|{
return|return
name|filter
return|;
block|}
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|filter
operator|.
name|reset
argument_list|()
expr_stmt|;
name|filterRow
operator|=
literal|false
expr_stmt|;
block|}
specifier|private
name|void
name|changeFR
parameter_list|(
name|boolean
name|value
parameter_list|)
block|{
name|filterRow
operator|=
name|filterRow
operator|||
name|value
expr_stmt|;
block|}
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
name|byte
index|[]
name|buffer
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|filterAllRemaining
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
name|KeyValue
name|v
parameter_list|)
block|{
name|ReturnCode
name|c
init|=
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|v
argument_list|)
decl_stmt|;
name|changeFR
argument_list|(
name|c
operator|!=
name|ReturnCode
operator|.
name|INCLUDE
argument_list|)
expr_stmt|;
return|return
name|c
return|;
block|}
specifier|public
name|boolean
name|filterRow
parameter_list|()
block|{
return|return
name|filterRow
return|;
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
name|Filter
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
literal|"Failed deserialize."
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
literal|"Failed deserialize."
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
literal|"Failed deserialize."
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

