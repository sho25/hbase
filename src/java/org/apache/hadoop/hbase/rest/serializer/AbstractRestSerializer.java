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
name|rest
operator|.
name|serializer
package|;
end_package

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServletResponse
import|;
end_import

begin_comment
comment|/**  *   *         Abstract object that is used as the base of all serializers in the  *         REST based interface.  */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|AbstractRestSerializer
implements|implements
name|IRestSerializer
block|{
comment|// keep the response object to write back to the stream
specifier|protected
specifier|final
name|HttpServletResponse
name|response
decl_stmt|;
comment|// Used to denote if pretty printing of the output should be used
specifier|protected
specifier|final
name|boolean
name|prettyPrint
decl_stmt|;
comment|/**    * marking the default constructor as private so it will never be used.    */
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
specifier|private
name|AbstractRestSerializer
parameter_list|()
block|{
name|response
operator|=
literal|null
expr_stmt|;
name|prettyPrint
operator|=
literal|false
expr_stmt|;
block|}
comment|/**    * Public constructor for AbstractRestSerializer. This is the constructor that    * should be called whenever creating a RestSerializer object.    *     * @param response    * @param prettyPrint     */
specifier|public
name|AbstractRestSerializer
parameter_list|(
name|HttpServletResponse
name|response
parameter_list|,
name|boolean
name|prettyPrint
parameter_list|)
block|{
name|super
argument_list|()
expr_stmt|;
name|this
operator|.
name|response
operator|=
name|response
expr_stmt|;
name|this
operator|.
name|prettyPrint
operator|=
name|prettyPrint
expr_stmt|;
block|}
block|}
end_class

end_unit

