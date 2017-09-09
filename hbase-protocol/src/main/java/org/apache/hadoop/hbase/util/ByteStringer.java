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
name|util
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
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|HBaseZeroCopyByteString
import|;
end_import

begin_comment
comment|/**  * Hack to workaround HBASE-10304 issue that keeps bubbling up when a mapreduce context.  */
end_comment

begin_comment
comment|// @InterfaceAudience.Private
end_comment

begin_comment
comment|// This class has NO InterfaceAudience. It is commented out. We do not want to import
end_comment

begin_comment
comment|// InterfaceAudience. This would be only class in this module with the IA import and we do not want
end_comment

begin_comment
comment|// to have this module depend annotations module just for one class.
end_comment

begin_comment
comment|// NO InterfaceAudience defaults to mean InterfaceAudience.Private!
end_comment

begin_class
specifier|public
class|class
name|ByteStringer
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ByteStringer
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Flag set at class loading time.    */
specifier|private
specifier|static
name|boolean
name|USE_ZEROCOPYBYTESTRING
init|=
literal|true
decl_stmt|;
comment|// Can I classload HBaseZeroCopyByteString without IllegalAccessError?
comment|// If we can, use it passing ByteStrings to pb else use native ByteString though more costly
comment|// because it makes a copy of the passed in array.
static|static
block|{
try|try
block|{
name|HBaseZeroCopyByteString
operator|.
name|wrap
argument_list|(
operator|new
name|byte
index|[
literal|0
index|]
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IllegalAccessError
name|iae
parameter_list|)
block|{
name|USE_ZEROCOPYBYTESTRING
operator|=
literal|false
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Failed to classload HBaseZeroCopyByteString: "
operator|+
name|iae
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|ByteStringer
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Wraps a byte array in a {@link ByteString} without copying it.    */
specifier|public
specifier|static
name|ByteString
name|wrap
parameter_list|(
specifier|final
name|byte
index|[]
name|array
parameter_list|)
block|{
return|return
name|USE_ZEROCOPYBYTESTRING
condition|?
name|HBaseZeroCopyByteString
operator|.
name|wrap
argument_list|(
name|array
argument_list|)
else|:
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|array
argument_list|)
return|;
block|}
comment|/**    * Wraps a subset of a byte array in a {@link ByteString} without copying it.    */
specifier|public
specifier|static
name|ByteString
name|wrap
parameter_list|(
specifier|final
name|byte
index|[]
name|array
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
return|return
name|USE_ZEROCOPYBYTESTRING
condition|?
name|HBaseZeroCopyByteString
operator|.
name|wrap
argument_list|(
name|array
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
else|:
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|array
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
return|;
block|}
block|}
end_class

end_unit

