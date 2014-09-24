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
name|security
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
name|security
operator|.
name|UserGroupInformation
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

begin_comment
comment|/** Authentication method */
end_comment

begin_enum
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
enum|enum
name|AuthMethod
block|{
name|SIMPLE
argument_list|(
operator|(
name|byte
operator|)
literal|80
argument_list|,
literal|""
argument_list|,
name|UserGroupInformation
operator|.
name|AuthenticationMethod
operator|.
name|SIMPLE
argument_list|)
block|,
name|KERBEROS
argument_list|(
operator|(
name|byte
operator|)
literal|81
argument_list|,
literal|"GSSAPI"
argument_list|,
name|UserGroupInformation
operator|.
name|AuthenticationMethod
operator|.
name|KERBEROS
argument_list|)
block|,
name|DIGEST
argument_list|(
operator|(
name|byte
operator|)
literal|82
argument_list|,
literal|"DIGEST-MD5"
argument_list|,
name|UserGroupInformation
operator|.
name|AuthenticationMethod
operator|.
name|TOKEN
argument_list|)
block|;
comment|/** The code for this method. */
specifier|public
specifier|final
name|byte
name|code
decl_stmt|;
specifier|public
specifier|final
name|String
name|mechanismName
decl_stmt|;
specifier|public
specifier|final
name|UserGroupInformation
operator|.
name|AuthenticationMethod
name|authenticationMethod
decl_stmt|;
name|AuthMethod
parameter_list|(
name|byte
name|code
parameter_list|,
name|String
name|mechanismName
parameter_list|,
name|UserGroupInformation
operator|.
name|AuthenticationMethod
name|authMethod
parameter_list|)
block|{
name|this
operator|.
name|code
operator|=
name|code
expr_stmt|;
name|this
operator|.
name|mechanismName
operator|=
name|mechanismName
expr_stmt|;
name|this
operator|.
name|authenticationMethod
operator|=
name|authMethod
expr_stmt|;
block|}
specifier|private
specifier|static
specifier|final
name|int
name|FIRST_CODE
init|=
name|values
argument_list|()
index|[
literal|0
index|]
operator|.
name|code
decl_stmt|;
comment|/** Return the object represented by the code. */
specifier|public
specifier|static
name|AuthMethod
name|valueOf
parameter_list|(
name|byte
name|code
parameter_list|)
block|{
specifier|final
name|int
name|i
init|=
operator|(
name|code
operator|&
literal|0xff
operator|)
operator|-
name|FIRST_CODE
decl_stmt|;
return|return
name|i
operator|<
literal|0
operator|||
name|i
operator|>=
name|values
argument_list|()
operator|.
name|length
condition|?
literal|null
else|:
name|values
argument_list|()
index|[
name|i
index|]
return|;
block|}
comment|/** Return the SASL mechanism name */
specifier|public
name|String
name|getMechanismName
parameter_list|()
block|{
return|return
name|mechanismName
return|;
block|}
comment|/** Read from in */
specifier|public
specifier|static
name|AuthMethod
name|read
parameter_list|(
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|valueOf
argument_list|(
name|in
operator|.
name|readByte
argument_list|()
argument_list|)
return|;
block|}
comment|/** Write to out */
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
name|write
argument_list|(
name|code
argument_list|)
expr_stmt|;
block|}
block|}
end_enum

end_unit

