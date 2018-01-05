begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright (C) 2007 The Guava Authors  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  *  */
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
name|io
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
operator|.
name|checkArgument
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
operator|.
name|checkNotNull
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FilterInputStream
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
name|InputStream
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
comment|/**  * Copied from guava source code v15 (LimitedInputStream)  * Guava deprecated LimitInputStream in v14 and removed it in v15. Copying this class here  * allows to be compatible with guava 11 to 15+.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|LimitInputStream
extends|extends
name|FilterInputStream
block|{
specifier|private
name|long
name|left
decl_stmt|;
specifier|private
name|long
name|mark
init|=
operator|-
literal|1
decl_stmt|;
specifier|public
name|LimitInputStream
parameter_list|(
name|InputStream
name|in
parameter_list|,
name|long
name|limit
parameter_list|)
block|{
name|super
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|checkNotNull
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|checkArgument
argument_list|(
name|limit
operator|>=
literal|0
argument_list|,
literal|"limit must be non-negative"
argument_list|)
expr_stmt|;
name|left
operator|=
name|limit
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|available
parameter_list|()
throws|throws
name|IOException
block|{
return|return
operator|(
name|int
operator|)
name|Math
operator|.
name|min
argument_list|(
name|in
operator|.
name|available
argument_list|()
argument_list|,
name|left
argument_list|)
return|;
block|}
comment|// it's okay to mark even if mark isn't supported, as reset won't work
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|mark
parameter_list|(
name|int
name|readLimit
parameter_list|)
block|{
name|in
operator|.
name|mark
argument_list|(
name|readLimit
argument_list|)
expr_stmt|;
name|mark
operator|=
name|left
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
name|left
operator|==
literal|0
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|int
name|result
init|=
name|in
operator|.
name|read
argument_list|()
decl_stmt|;
if|if
condition|(
name|result
operator|!=
operator|-
literal|1
condition|)
block|{
operator|--
name|left
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|read
parameter_list|(
name|byte
index|[]
name|b
parameter_list|,
name|int
name|off
parameter_list|,
name|int
name|len
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|left
operator|==
literal|0
condition|)
block|{
return|return
operator|-
literal|1
return|;
block|}
name|len
operator|=
operator|(
name|int
operator|)
name|Math
operator|.
name|min
argument_list|(
name|len
argument_list|,
name|left
argument_list|)
expr_stmt|;
name|int
name|result
init|=
name|in
operator|.
name|read
argument_list|(
name|b
argument_list|,
name|off
argument_list|,
name|len
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
operator|-
literal|1
condition|)
block|{
name|left
operator|-=
name|result
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|in
operator|.
name|markSupported
argument_list|()
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Mark not supported"
argument_list|)
throw|;
block|}
if|if
condition|(
name|mark
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Mark not set"
argument_list|)
throw|;
block|}
name|in
operator|.
name|reset
argument_list|()
expr_stmt|;
name|left
operator|=
name|mark
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|skip
parameter_list|(
name|long
name|n
parameter_list|)
throws|throws
name|IOException
block|{
name|n
operator|=
name|Math
operator|.
name|min
argument_list|(
name|n
argument_list|,
name|left
argument_list|)
expr_stmt|;
name|long
name|skipped
init|=
name|in
operator|.
name|skip
argument_list|(
name|n
argument_list|)
decl_stmt|;
name|left
operator|-=
name|skipped
expr_stmt|;
return|return
name|skipped
return|;
block|}
block|}
end_class

end_unit

