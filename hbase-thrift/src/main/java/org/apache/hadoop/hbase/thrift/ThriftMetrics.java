begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|thrift
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
name|CallQueueTooBigException
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
name|MultiActionResultTooLarge
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
name|NotServingRegionException
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
name|RegionTooBusyException
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
name|UnknownScannerException
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
name|CompatibilitySingletonFactory
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
name|exceptions
operator|.
name|ClientExceptionsUtil
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
name|exceptions
operator|.
name|FailedSanityCheckException
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
name|exceptions
operator|.
name|OutOfOrderScannerNextException
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
name|exceptions
operator|.
name|RegionMovedException
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
name|exceptions
operator|.
name|ScannerResetException
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
name|thrift
operator|.
name|generated
operator|.
name|IOError
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
name|thrift2
operator|.
name|generated
operator|.
name|TIOError
import|;
end_import

begin_comment
comment|/**  * This class is for maintaining the various statistics of thrift server  * and publishing them through the metrics interfaces.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ThriftMetrics
block|{
specifier|public
enum|enum
name|ThriftServerType
block|{
name|ONE
block|,
name|TWO
block|}
specifier|public
name|MetricsThriftServerSource
name|getSource
parameter_list|()
block|{
return|return
name|source
return|;
block|}
specifier|public
name|void
name|setSource
parameter_list|(
name|MetricsThriftServerSource
name|source
parameter_list|)
block|{
name|this
operator|.
name|source
operator|=
name|source
expr_stmt|;
block|}
specifier|private
name|MetricsThriftServerSource
name|source
decl_stmt|;
specifier|private
specifier|final
name|long
name|slowResponseTime
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|SLOW_RESPONSE_NANO_SEC
init|=
literal|"hbase.thrift.slow.response.nano.second"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_SLOW_RESPONSE_NANO_SEC
init|=
literal|10
operator|*
literal|1000
operator|*
literal|1000
decl_stmt|;
specifier|public
name|ThriftMetrics
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|ThriftServerType
name|t
parameter_list|)
block|{
name|slowResponseTime
operator|=
name|conf
operator|.
name|getLong
argument_list|(
name|SLOW_RESPONSE_NANO_SEC
argument_list|,
name|DEFAULT_SLOW_RESPONSE_NANO_SEC
argument_list|)
expr_stmt|;
if|if
condition|(
name|t
operator|==
name|ThriftServerType
operator|.
name|ONE
condition|)
block|{
name|source
operator|=
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsThriftServerSourceFactory
operator|.
name|class
argument_list|)
operator|.
name|createThriftOneSource
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|t
operator|==
name|ThriftServerType
operator|.
name|TWO
condition|)
block|{
name|source
operator|=
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsThriftServerSourceFactory
operator|.
name|class
argument_list|)
operator|.
name|createThriftTwoSource
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|incTimeInQueue
parameter_list|(
name|long
name|time
parameter_list|)
block|{
name|source
operator|.
name|incTimeInQueue
argument_list|(
name|time
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setCallQueueLen
parameter_list|(
name|int
name|len
parameter_list|)
block|{
name|source
operator|.
name|setCallQueueLen
argument_list|(
name|len
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|incNumRowKeysInBatchGet
parameter_list|(
name|int
name|diff
parameter_list|)
block|{
name|source
operator|.
name|incNumRowKeysInBatchGet
argument_list|(
name|diff
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|incNumRowKeysInBatchMutate
parameter_list|(
name|int
name|diff
parameter_list|)
block|{
name|source
operator|.
name|incNumRowKeysInBatchMutate
argument_list|(
name|diff
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|incMethodTime
parameter_list|(
name|String
name|name
parameter_list|,
name|long
name|time
parameter_list|)
block|{
name|source
operator|.
name|incMethodTime
argument_list|(
name|name
argument_list|,
name|time
argument_list|)
expr_stmt|;
comment|// inc general processTime
name|source
operator|.
name|incCall
argument_list|(
name|time
argument_list|)
expr_stmt|;
if|if
condition|(
name|time
operator|>
name|slowResponseTime
condition|)
block|{
name|source
operator|.
name|incSlowCall
argument_list|(
name|time
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|incActiveWorkerCount
parameter_list|()
block|{
name|source
operator|.
name|incActiveWorkerCount
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|decActiveWorkerCount
parameter_list|()
block|{
name|source
operator|.
name|decActiveWorkerCount
argument_list|()
expr_stmt|;
block|}
comment|/**    * Increment the count for a specific exception type.  This is called for each exception type    * that is returned to the thrift handler.    * @param rawThrowable type of exception    */
specifier|public
name|void
name|exception
parameter_list|(
name|Throwable
name|rawThrowable
parameter_list|)
block|{
name|source
operator|.
name|exception
argument_list|()
expr_stmt|;
name|Throwable
name|throwable
init|=
name|unwrap
argument_list|(
name|rawThrowable
argument_list|)
decl_stmt|;
comment|/**      * Keep some metrics for commonly seen exceptions      *      * Try and  put the most common types first.      * Place child types before the parent type that they extend.      *      * If this gets much larger we might have to go to a hashmap      */
if|if
condition|(
name|throwable
operator|!=
literal|null
condition|)
block|{
if|if
condition|(
name|throwable
operator|instanceof
name|OutOfOrderScannerNextException
condition|)
block|{
name|source
operator|.
name|outOfOrderException
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|throwable
operator|instanceof
name|RegionTooBusyException
condition|)
block|{
name|source
operator|.
name|tooBusyException
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|throwable
operator|instanceof
name|UnknownScannerException
condition|)
block|{
name|source
operator|.
name|unknownScannerException
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|throwable
operator|instanceof
name|ScannerResetException
condition|)
block|{
name|source
operator|.
name|scannerResetException
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|throwable
operator|instanceof
name|RegionMovedException
condition|)
block|{
name|source
operator|.
name|movedRegionException
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|throwable
operator|instanceof
name|NotServingRegionException
condition|)
block|{
name|source
operator|.
name|notServingRegionException
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|throwable
operator|instanceof
name|FailedSanityCheckException
condition|)
block|{
name|source
operator|.
name|failedSanityException
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|throwable
operator|instanceof
name|MultiActionResultTooLarge
condition|)
block|{
name|source
operator|.
name|multiActionTooLargeException
argument_list|()
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|throwable
operator|instanceof
name|CallQueueTooBigException
condition|)
block|{
name|source
operator|.
name|callQueueTooBigException
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
name|Throwable
name|unwrap
parameter_list|(
name|Throwable
name|t
parameter_list|)
block|{
if|if
condition|(
name|t
operator|==
literal|null
condition|)
block|{
return|return
name|t
return|;
block|}
if|if
condition|(
name|t
operator|instanceof
name|TIOError
operator|||
name|t
operator|instanceof
name|IOError
condition|)
block|{
name|t
operator|=
name|t
operator|.
name|getCause
argument_list|()
expr_stmt|;
block|}
return|return
name|ClientExceptionsUtil
operator|.
name|findException
argument_list|(
name|t
argument_list|)
return|;
block|}
block|}
end_class

end_unit

