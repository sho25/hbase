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
name|ipc
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
name|client
operator|.
name|*
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
name|coprocessor
operator|.
name|Exec
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
name|coprocessor
operator|.
name|ExecResult
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
name|lang
operator|.
name|reflect
operator|.
name|InvocationHandler
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
comment|/**  * Backs a {@link CoprocessorProtocol} subclass proxy and forwards method  * invocations for server execution.  Note that internally this will issue a  * separate RPC call for each method invocation (using a  * {@link org.apache.hadoop.hbase.client.ServerCallable} instance).  */
end_comment

begin_class
specifier|public
class|class
name|ExecRPCInvoker
implements|implements
name|InvocationHandler
block|{
comment|// LOG is NOT in hbase subpackage intentionally so that the default HBase
comment|// DEBUG log level does NOT emit RPC-level logging.
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
literal|"org.apache.hadoop.ipc.ExecRPCInvoker"
argument_list|)
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
specifier|private
specifier|final
name|HConnection
name|connection
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
specifier|private
specifier|final
name|byte
index|[]
name|table
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|row
decl_stmt|;
specifier|private
name|byte
index|[]
name|regionName
decl_stmt|;
specifier|public
name|ExecRPCInvoker
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HConnection
name|connection
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|CoprocessorProtocol
argument_list|>
name|protocol
parameter_list|,
name|byte
index|[]
name|table
parameter_list|,
name|byte
index|[]
name|row
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
name|this
operator|.
name|connection
operator|=
name|connection
expr_stmt|;
name|this
operator|.
name|protocol
operator|=
name|protocol
expr_stmt|;
name|this
operator|.
name|table
operator|=
name|table
expr_stmt|;
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Object
name|invoke
parameter_list|(
name|Object
name|instance
parameter_list|,
specifier|final
name|Method
name|method
parameter_list|,
specifier|final
name|Object
index|[]
name|args
parameter_list|)
throws|throws
name|Throwable
block|{
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Call: "
operator|+
name|method
operator|.
name|getName
argument_list|()
operator|+
literal|", "
operator|+
operator|(
name|args
operator|!=
literal|null
condition|?
name|args
operator|.
name|length
else|:
literal|0
operator|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|row
operator|!=
literal|null
condition|)
block|{
specifier|final
name|Exec
name|exec
init|=
operator|new
name|Exec
argument_list|(
name|conf
argument_list|,
name|row
argument_list|,
name|protocol
argument_list|,
name|method
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|ServerCallable
argument_list|<
name|ExecResult
argument_list|>
name|callable
init|=
operator|new
name|ServerCallable
argument_list|<
name|ExecResult
argument_list|>
argument_list|(
name|connection
argument_list|,
name|table
argument_list|,
name|row
argument_list|)
block|{
specifier|public
name|ExecResult
name|call
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|server
operator|.
name|execCoprocessor
argument_list|(
name|location
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|,
name|exec
argument_list|)
return|;
block|}
block|}
decl_stmt|;
name|ExecResult
name|result
init|=
name|callable
operator|.
name|withRetries
argument_list|()
decl_stmt|;
name|this
operator|.
name|regionName
operator|=
name|result
operator|.
name|getRegionName
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Result is region="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|regionName
argument_list|)
operator|+
literal|", value="
operator|+
name|result
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|result
operator|.
name|getValue
argument_list|()
return|;
block|}
return|return
literal|null
return|;
block|}
specifier|public
name|byte
index|[]
name|getRegionName
parameter_list|()
block|{
return|return
name|regionName
return|;
block|}
block|}
end_class

end_unit

