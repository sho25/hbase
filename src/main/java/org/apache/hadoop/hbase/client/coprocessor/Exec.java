begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
operator|.
name|coprocessor
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
name|conf
operator|.
name|Configuration
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
name|HBaseConfiguration
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
name|client
operator|.
name|Row
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
name|HbaseObjectWritable
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
name|ipc
operator|.
name|CoprocessorProtocol
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
name|ipc
operator|.
name|Invocation
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
name|Bytes
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

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
import|;
end_import

begin_comment
comment|/**  * Represents an arbitrary method invocation against a Coprocessor  * instance.  In order for a coprocessor implementation to be remotely callable  * by clients, it must define and implement a {@link CoprocessorProtocol}  * subclass.  Only methods defined in the {@code CoprocessorProtocol} interface  * will be callable by clients.  *  *<p>  * This class is used internally by  * {@link org.apache.hadoop.hbase.client.HTable#coprocessorExec(Class, byte[], byte[], org.apache.hadoop.hbase.client.coprocessor.Batch.Call, org.apache.hadoop.hbase.client.coprocessor.Batch.Callback)}  * to wrap the {@code CoprocessorProtocol} method invocations requested in  * RPC calls.  It should not be used directly by HBase clients.  *</p>  *  * @see ExecResult  * @see org.apache.hadoop.hbase.client.HTable#coprocessorExec(Class, byte[], byte[], org.apache.hadoop.hbase.client.coprocessor.Batch.Call)  * @see org.apache.hadoop.hbase.client.HTable#coprocessorExec(Class, byte[], byte[], org.apache.hadoop.hbase.client.coprocessor.Batch.Call, org.apache.hadoop.hbase.client.coprocessor.Batch.Callback)  */
end_comment

begin_class
specifier|public
class|class
name|Exec
extends|extends
name|Invocation
implements|implements
name|Row
block|{
specifier|private
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
comment|/** Row key used as a reference for any region lookups */
specifier|private
name|byte
index|[]
name|referenceRow
decl_stmt|;
specifier|private
name|Class
argument_list|<
name|?
extends|extends
name|CoprocessorProtocol
argument_list|>
name|protocol
decl_stmt|;
specifier|public
name|Exec
parameter_list|()
block|{   }
specifier|public
name|Exec
parameter_list|(
name|Configuration
name|configuration
parameter_list|,
name|byte
index|[]
name|row
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|CoprocessorProtocol
argument_list|>
name|protocol
parameter_list|,
name|Method
name|method
parameter_list|,
name|Object
index|[]
name|parameters
parameter_list|)
block|{
name|super
argument_list|(
name|method
argument_list|,
name|parameters
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|configuration
expr_stmt|;
name|this
operator|.
name|referenceRow
operator|=
name|row
expr_stmt|;
name|this
operator|.
name|protocol
operator|=
name|protocol
expr_stmt|;
block|}
specifier|public
name|Class
argument_list|<
name|?
extends|extends
name|CoprocessorProtocol
argument_list|>
name|getProtocol
parameter_list|()
block|{
return|return
name|protocol
return|;
block|}
specifier|public
name|byte
index|[]
name|getRow
parameter_list|()
block|{
return|return
name|referenceRow
return|;
block|}
specifier|public
name|int
name|compareTo
parameter_list|(
name|Row
name|row
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|compareTo
argument_list|(
name|referenceRow
argument_list|,
name|row
operator|.
name|getRow
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
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
name|super
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|referenceRow
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|protocol
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
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
name|super
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|referenceRow
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|String
name|protocolName
init|=
name|in
operator|.
name|readUTF
argument_list|()
decl_stmt|;
try|try
block|{
name|protocol
operator|=
operator|(
name|Class
argument_list|<
name|CoprocessorProtocol
argument_list|>
operator|)
name|conf
operator|.
name|getClassByName
argument_list|(
name|protocolName
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ClassNotFoundException
name|cnfe
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Protocol class "
operator|+
name|protocolName
operator|+
literal|" not found"
argument_list|,
name|cnfe
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

