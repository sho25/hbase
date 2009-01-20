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
package|;
end_package

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
name|lang
operator|.
name|reflect
operator|.
name|Constructor
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
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
name|ipc
operator|.
name|RemoteException
import|;
end_import

begin_comment
comment|/**   * An immutable class which contains a static method for handling  * org.apache.hadoop.ipc.RemoteException exceptions.  */
end_comment

begin_class
specifier|public
class|class
name|RemoteExceptionHandler
block|{
comment|/* Not instantiable */
specifier|private
name|RemoteExceptionHandler
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Examine passed Throwable.  See if its carrying a RemoteException. If so,    * run {@link #decodeRemoteException(RemoteException)} on it.  Otherwise,    * pass back<code>t</code> unaltered.    * @param t Throwable to examine.    * @return Decoded RemoteException carried by<code>t</code> or    *<code>t</code> unaltered.    */
specifier|public
specifier|static
name|Throwable
name|checkThrowable
parameter_list|(
specifier|final
name|Throwable
name|t
parameter_list|)
block|{
name|Throwable
name|result
init|=
name|t
decl_stmt|;
if|if
condition|(
name|t
operator|instanceof
name|RemoteException
condition|)
block|{
try|try
block|{
name|result
operator|=
name|RemoteExceptionHandler
operator|.
name|decodeRemoteException
argument_list|(
operator|(
name|RemoteException
operator|)
name|t
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Throwable
name|tt
parameter_list|)
block|{
name|result
operator|=
name|tt
expr_stmt|;
block|}
block|}
return|return
name|result
return|;
block|}
comment|/**    * Examine passed IOException.  See if its carrying a RemoteException. If so,    * run {@link #decodeRemoteException(RemoteException)} on it.  Otherwise,    * pass back<code>e</code> unaltered.    * @param e Exception to examine.    * @return Decoded RemoteException carried by<code>e</code> or    *<code>e</code> unaltered.    */
specifier|public
specifier|static
name|IOException
name|checkIOException
parameter_list|(
specifier|final
name|IOException
name|e
parameter_list|)
block|{
name|Throwable
name|t
init|=
name|checkThrowable
argument_list|(
name|e
argument_list|)
decl_stmt|;
return|return
name|t
operator|instanceof
name|IOException
condition|?
operator|(
name|IOException
operator|)
name|t
else|:
operator|new
name|IOException
argument_list|(
name|t
argument_list|)
return|;
block|}
comment|/**    * Converts org.apache.hadoop.ipc.RemoteException into original exception,    * if possible. If the original exception is an Error or a RuntimeException,    * throws the original exception.    *     * @param re original exception    * @return decoded RemoteException if it is an instance of or a subclass of    *         IOException, or the original RemoteException if it cannot be decoded.    *     * @throws IOException indicating a server error ocurred if the decoded     *         exception is not an IOException. The decoded exception is set as    *         the cause.    */
specifier|public
specifier|static
name|IOException
name|decodeRemoteException
parameter_list|(
specifier|final
name|RemoteException
name|re
parameter_list|)
throws|throws
name|IOException
block|{
name|IOException
name|i
init|=
name|re
decl_stmt|;
try|try
block|{
name|Class
argument_list|<
name|?
argument_list|>
name|c
init|=
name|Class
operator|.
name|forName
argument_list|(
name|re
operator|.
name|getClassName
argument_list|()
argument_list|)
decl_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
index|[]
name|parameterTypes
init|=
block|{
name|String
operator|.
name|class
block|}
decl_stmt|;
name|Constructor
argument_list|<
name|?
argument_list|>
name|ctor
init|=
name|c
operator|.
name|getConstructor
argument_list|(
name|parameterTypes
argument_list|)
decl_stmt|;
name|Object
index|[]
name|arguments
init|=
block|{
name|re
operator|.
name|getMessage
argument_list|()
block|}
decl_stmt|;
name|Throwable
name|t
init|=
operator|(
name|Throwable
operator|)
name|ctor
operator|.
name|newInstance
argument_list|(
name|arguments
argument_list|)
decl_stmt|;
if|if
condition|(
name|t
operator|instanceof
name|IOException
condition|)
block|{
name|i
operator|=
operator|(
name|IOException
operator|)
name|t
expr_stmt|;
block|}
else|else
block|{
name|i
operator|=
operator|new
name|IOException
argument_list|(
literal|"server error"
argument_list|)
expr_stmt|;
name|i
operator|.
name|initCause
argument_list|(
name|t
argument_list|)
expr_stmt|;
throw|throw
name|i
throw|;
block|}
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|x
parameter_list|)
block|{
comment|// continue
block|}
catch|catch
parameter_list|(
name|NoSuchMethodException
name|x
parameter_list|)
block|{
comment|// continue
block|}
catch|catch
parameter_list|(
name|IllegalAccessException
name|x
parameter_list|)
block|{
comment|// continue
block|}
catch|catch
parameter_list|(
name|InvocationTargetException
name|x
parameter_list|)
block|{
comment|// continue
block|}
catch|catch
parameter_list|(
name|InstantiationException
name|x
parameter_list|)
block|{
comment|// continue
block|}
return|return
name|i
return|;
block|}
block|}
end_class

end_unit

