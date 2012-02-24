/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.jersey.spi.container;

import com.sun.jersey.core.header.OutBoundHeaders;
import com.sun.jersey.api.Responses;
import com.sun.jersey.api.container.MappableContainerException;
import com.sun.jersey.api.core.HttpResponseContext;
import com.sun.jersey.api.core.TraceInformation;
import com.sun.jersey.core.reflection.ReflectionHelper;
import com.sun.jersey.core.spi.factory.ResponseBuilderHeaders;
import com.sun.jersey.core.spi.factory.ResponseImpl;
import com.sun.jersey.spi.MessageBodyWorkers;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 * An out-bound HTTP response to be processed by the web application.
 * <p>
 * Containers instantiate, or inherit, and provide an instance to the
 * {@link WebApplication}.
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ContainerResponse implements HttpResponseContext {
    private static final Annotation[] EMPTY_ANNOTATIONS = new Annotation[0];
    
    private static final Logger LOGGER = Logger.getLogger(ContainerResponse.class.getName());
    
    private static final RuntimeDelegate rd = RuntimeDelegate.getInstance();
    
    private final WebApplication wa;
    
    private ContainerRequest request;
    
    private ContainerResponseWriter responseWriter;

    private Response response;

    private Throwable mappedThrowable;
    
    private int status;
    
    private MultivaluedMap<String, Object> headers;
    
    private Object originalEntity;

    private Object entity;
    
    private Type entityType;
    
    private boolean isCommitted;
    
    private CommittingOutputStream out;
    
    private Annotation[] annotations = EMPTY_ANNOTATIONS;
    
    private final class CommittingOutputStream extends OutputStream {
        private final long size;
        
        private OutputStream o;

        CommittingOutputStream(long size) {
            this.size = size;
        }
        
        @Override
        public void write(byte b[]) throws IOException {
            commitWrite();
            o.write(b);
        }
        
        @Override
        public void write(byte b[], int off, int len) throws IOException {
            commitWrite();
            o.write(b, off, len);
        }
        
        public void write(int b) throws IOException {
            commitWrite();
            o.write(b);
        }
        
        @Override
        public void flush() throws IOException {
            commitWrite();
            o.flush();
        }
        
        @Override
        public void close() throws IOException {
            commitClose();
            o.close();
        }
        
        private void commitWrite() throws IOException {
            if (!isCommitted) {
                if (getStatus() == 204)
                    setStatus(200);
                isCommitted = true;
                o = responseWriter.writeStatusAndHeaders(size, ContainerResponse.this);
            }
        }
        
        private void commitClose() throws IOException {
            if (!isCommitted) {
                isCommitted = true;
                o = responseWriter.writeStatusAndHeaders(-1, ContainerResponse.this);
            }
        }
    };

    /**
     * Instantate a new ContainerResponse.
     * 
     * @param wa the web application.
     * @param request the container request associated with this response.
     * @param responseWriter the response writer
     */
    public ContainerResponse(
            WebApplication wa, 
            ContainerRequest request,
            ContainerResponseWriter responseWriter) {
        this.wa = wa;
        this.request = request;
        this.responseWriter = responseWriter;
        this.status = Responses.NO_CONTENT;
    }
    
    /*package */ ContainerResponse(
            ContainerResponse acr) {
        this.wa = acr.wa;
    }

    // ContainerResponse
        
    /**
     * Convert a header value, represented as a general object, to the 
     * string value.
     * <p>
     * This method defers to {@link RuntimeDelegate#createHeaderDelegate} to
     * obtain a {@link HeaderDelegate} to convert the value to a string. If
     * a {@link HeaderDelegate} is not found then the <code>toString</code>
     * is utilized.
     * <p>
     * Containers may use this method to convert the header values obtained
     * from the {@link #getHttpHeaders}
     * 
     * @param headerValue the header value as an object
     * @return the string value
     */
    public static String getHeaderValue(Object headerValue) {
        HeaderDelegate hp = rd.createHeaderDelegate(headerValue.getClass());
        
        return (hp != null) ? hp.toString(headerValue) : headerValue.toString();
    }
    
    /**
     * Write the response.
     * <p>
     * The status and headers will be written by calling the method
     * {@link ContainerResponseWriter#writeStatusAndHeaders} on the provided
     * {@link ContainerResponseWriter} instance. The {@link OutputStream}
     * returned from that method call is used to write the entity (if any)
     * to that {@link OutputStream}. An appropriate {@link MessageBodyWriter}
     * will be found to write the entity.
     * 
     * @throws WebApplicationException if {@link MessageBodyWriter} cannot be 
     *         found for the entity with a 500 (Internal Server error) response.
     * @throws java.io.IOException if there is an error writing the entity
     */
    public void write() throws IOException {
        if (isCommitted)
            return;        

        if (request.isTracingEnabled()) {
            configureTrace(responseWriter);
        }

        if (entity == null) {
            isCommitted = true;
            responseWriter.writeStatusAndHeaders(-1, this);
            responseWriter.finish();
            return;
        }
        
        MediaType contentType = getMediaType();
        if (contentType == null) {
            contentType = getMessageBodyWorkers().getMessageBodyWriterMediaType(
                        entity.getClass(),
                        entityType,
                        annotations,
                        request.getAcceptableMediaTypes());
            if (contentType == null ||
                    contentType.isWildcardType() || contentType.isWildcardSubtype())
                contentType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
            
            getHttpHeaders().putSingle("Content-Type", contentType);
        }
        
        final MessageBodyWriter p = getMessageBodyWorkers().getMessageBodyWriter(
                entity.getClass(), entityType, 
                annotations, contentType);
        if (p == null) {
            LOGGER.severe("A message body writer for Java type, " + entity.getClass() +
                    ", and MIME media type, " + contentType + ", was not found");
            
            if (request.getMethod().equals("HEAD")) {
                isCommitted = true;
                responseWriter.writeStatusAndHeaders(-1, this);
                responseWriter.finish();
                return;
            } else {
                throw new WebApplicationException(500);
            }
        }

        final long size = p.getSize(entity, entity.getClass(), entityType, 
                annotations, contentType);
        if (request.getMethod().equals("HEAD")) {
            if (size != -1)
                getHttpHeaders().putSingle("Content-Length", Long.toString(size));
            isCommitted = true;
            responseWriter.writeStatusAndHeaders(0, this);
        } else {
            if (request.isTracingEnabled()) {
                request.trace(String.format("matched message body writer: %s, \"%s\" -> %s",
                        ReflectionHelper.objectToString(entity),
                        contentType,
                        ReflectionHelper.objectToString(p)));
            }

            if (out == null)
                out = new CommittingOutputStream(size);
            p.writeTo(entity, entity.getClass(), entityType,
                    annotations, contentType, getHttpHeaders(),
                    out);
            if (!isCommitted) {
                isCommitted = true;
                responseWriter.writeStatusAndHeaders(-1, this);
            }
        }
        responseWriter.finish();
    }

    private void configureTrace(final ContainerResponseWriter crw) {
        final TraceInformation ti = (TraceInformation)request.getProperties().
                get(TraceInformation.class.getName());
        setContainerResponseWriter(new ContainerResponseWriter() {
            public OutputStream writeStatusAndHeaders(long contentLength,
                    ContainerResponse response) throws IOException {
                ti.addTraceHeaders();
                return crw.writeStatusAndHeaders(contentLength, response);
            }

            public void finish() throws IOException {
                crw.finish();
            }
        });
    }

    /**
     * Reset the response to 204 (No content) with no headers.
     */
    public void reset() {
        setResponse(Responses.noContent().build());
    }

    /**
     * Get the container request.
     *
     * @return the container request.
     */
    public ContainerRequest getContainerRequest() {
        return request;
    }

    /**
     * Set the container request.
     *
     * @param request the container request.
     */
    public void setContainerRequest(ContainerRequest request) {
        this.request = request;
    }
    
    /**
     * Get the container response writer.
     * 
     * @return the container response writer
     */
    public ContainerResponseWriter getContainerResponseWriter() {
        return responseWriter; 
    }
    
    /**
     * Set the container response writer.
     * 
     * @param responseWriter the container response writer
     */
    public void setContainerResponseWriter(ContainerResponseWriter responseWriter) {
        this.responseWriter = responseWriter; 
    }

    /**
     * Get the message body workers.
     *
     * @return the message body workers.
     */
    public MessageBodyWorkers getMessageBodyWorkers() {
        return wa.getMessageBodyWorkers();
    }

    /**
     * Map the cause of a mapable container exception to a response.
     * <p>
     * If the cause cannot be mapped and then that cause is re-thrown
     * if a runtime exception otherwise the mappable contaner exception is
     * re-thrown.
     * 
     * @param e the mappable container exception whose cause will be mapped to
     *        a response.
     */
    public void mapMappableContainerException(MappableContainerException e) {
        Throwable cause = e.getCause();

        if (cause instanceof WebApplicationException) {
            mapWebApplicationException((WebApplicationException)cause);
        } else if (!mapException(cause)) {
            if (cause instanceof RuntimeException) {
                LOGGER.log(Level.SEVERE, "The RuntimeException could not be mapped to a response, " +
                        "re-throwing to the HTTP container", cause);
                throw (RuntimeException)cause;
            } else {
                LOGGER.log(Level.SEVERE, "The exception contained within " +
                        "MappableContainerException could not be mapped to a response, " +
                        "re-throwing to the HTTP container", cause);
                throw e;
            }
        }
    }

    /**
     * Map a web application exception to a response.
     *
     * @param e the web application exception.
     */
    public void mapWebApplicationException(WebApplicationException e) {
        if (e.getResponse().getEntity() != null) {
            onException(e, e.getResponse(), false);
        } else {
            if (!mapException(e)) {
                onException(e, e.getResponse(), false);
            }
        }
    }

    /**
     * Map an exception to a response.
     *
     * @param e the exception.
     * @return true if the exception was mapped, otherwise false.
     */
    public boolean mapException(Throwable e) {
        ExceptionMapper em = wa.getExceptionMapperContext().find(e.getClass());
        if (em == null) return false;

        if (request.isTracingEnabled()) {
            request.trace(String.format("matched exception mapper: %s -> %s",
                    ReflectionHelper.objectToString(e),
                    ReflectionHelper.objectToString(em)));
        }

        try {
            Response r = em.toResponse(e);
            if (r == null)
                r = Response.noContent().build();
            onException(e, r, true);
        } catch (MappableContainerException ex) {
            // If the exception mapper throws a MappableContainerException then
            // rethrow it to the HTTP container
            throw ex;
        } catch (RuntimeException ex) {
            LOGGER.severe("Exception mapper " + em +
                    " for Throwable " + e +
                    " threw a RuntimeException when " +
                    "attempting to obtain the response");
            Response r = Response.serverError().build();
            onException(ex, r, false);
        }
        return true;
    }

    private void onException(Throwable e, Response r, boolean mapped) {
        if (!mapped) {
            // Log the stack trace
            if (r.getStatus() >= 500 || request.isTracingEnabled()) {
                traceException(e, r);
            }

            if (r.getStatus() >= 500 && r.getEntity() == null) {
                // Write out the exception to a string
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                pw.flush();

                r = Response.status(r.getStatus()).entity(sw.toString()).
                        type("text/plain").build();
            }
        } else if (request.isTracingEnabled()) {
            traceException(e, r);
        }

        setResponse(r);
        this.mappedThrowable = e;
    }

    private void traceException(Throwable e, Response r) {
        Response.Status s = Response.Status.fromStatusCode(r.getStatus());
        Level l = (r.getStatus() >= 500) ? Level.SEVERE : Level.INFO;
        if (s != null) {
            LOGGER.log(l,
                    "Mapped exception to response: " + r.getStatus() + " (" + s.getReasonPhrase() + ")",
                    e);
        } else {
            LOGGER.log(l,
                    "Mapped exception to response: " + r.getStatus(),
                    e);
        }
    }

    // HttpResponseContext
    
    public Response getResponse() {
        if (response == null) {
            setResponse(null);
        }
        
        return response;
    }
    
    public void setResponse(Response response) {
        this.isCommitted = false;
        this.out = null;
        this.response = response = (response != null) ? response : Responses.noContent().build();
        this.mappedThrowable = null;
        
        this.status = response.getStatus();
        
        if (response instanceof ResponseImpl) {
            this.headers = setResponseOptimal((ResponseImpl)response);
        } else {
            this.headers = setResponseNonOptimal(response);
        }
    }
    
    public boolean isResponseSet() {
        return response != null;
    }
    
    public Throwable getMappedThrowable() {
        return mappedThrowable;
    }

    public int getStatus() {
        return status;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }
    
    public Object getEntity() {
        return entity;
    }
    
    public Type getEntityType() {
        return entityType;
    }
    
    public Object getOriginalEntity() {
        return originalEntity;
    }

    public void setEntity(Object entity) {
        this.originalEntity = this.entity = entity;
        if (this.entity instanceof GenericEntity) {
            final GenericEntity ge = (GenericEntity)this.entity;
            this.entityType = ge.getType();                
            this.entity = ge.getEntity();            
        } else if (entity != null) {
            this.entityType = this.entity.getClass();
        }        
        
        checkStatusAndEntity();
    }
    
    public Annotation[] getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Annotation[] annotations) {
        this.annotations = (annotations != null) ? annotations : EMPTY_ANNOTATIONS;
    }
    
    public MultivaluedMap<String, Object> getHttpHeaders() {
        if (headers == null)
            headers = new OutBoundHeaders();
        return headers;
    }
    
    public MediaType getMediaType() {
        final Object mediaTypeHeader = getHttpHeaders().getFirst("Content-Type");
        if (mediaTypeHeader instanceof MediaType) {
            return (MediaType)mediaTypeHeader;
        } else if (mediaTypeHeader != null) {
            return MediaType.valueOf(mediaTypeHeader.toString());
        }

        return null;
    }
    
    public OutputStream getOutputStream() throws IOException {
        if (out == null)
            out = new CommittingOutputStream(-1);
        
        return out;
    }
    
    public boolean isCommitted() {
        return isCommitted;
    }
    
    
    //

    private void checkStatusAndEntity() {
        if (status == 204 && entity != null) status = 200;
        else if (status == 200 && entity == null) status = 204;
    }
    
    private MultivaluedMap<String, Object> setResponseOptimal(ResponseImpl r) {
        if (r.isMetatadataSet())
            return setResponseNonOptimal(r);
        
        this.originalEntity = this.entity = r.getEntity();
        this.entityType = r.getEntityType();
        if (entity instanceof GenericEntity) {
            final GenericEntity ge = (GenericEntity)this.entity;
            this.entityType = ge.getType();                
            this.entity = ge.getEntity();
        }
        
        return getMetadataOptimal(r.getValues(), r.getNameValuePairs());
    }

    private MultivaluedMap<String, Object> getMetadataOptimal(Object[] values,
            List<Object> nameValuePairs) {

        MultivaluedMap<String, Object> _headers = new OutBoundHeaders();

        for (int i = 0; i < values.length; i++) {
            if (i != ResponseBuilderHeaders.LOCATION) {
                if (values[i] != null)
                    _headers.putSingle(ResponseBuilderHeaders.getNameFromId(i), values[i]);
            } else {
                Object location = values[i];
                if (location != null) {
                    if (location instanceof URI) {
                        final URI locationUri = (URI)location;
                        if (!locationUri.isAbsolute()) {
                            final URI base = (status == 201)
                                    ? request.getAbsolutePath()
                                    : request.getBaseUri();
                            location = UriBuilder.fromUri(base).
                                    path(locationUri.getRawPath()).
                                    replaceQuery(locationUri.getRawQuery()).
                                    fragment(locationUri.getRawFragment()).
                                    build();
                        }
                    }
                    _headers.putSingle(HttpHeaders.LOCATION, location);
                }
            }
        }

        if (nameValuePairs.size() > 0) {
            Iterator i = nameValuePairs.iterator();
            while (i.hasNext()) {
                _headers.add((String)i.next(), i.next());
            }
        }

        return _headers;
    }

    private MultivaluedMap<String, Object> setResponseNonOptimal(Response r) {
        setEntity(r.getEntity());
        
        MultivaluedMap<String, Object> _headers = r.getMetadata();
        
        Object location = _headers.getFirst(HttpHeaders.LOCATION);
        if (location != null) {
            if (location instanceof URI) {
                final URI locationUri = (URI)location;
                if (!locationUri.isAbsolute()) {
                    final URI base = (status == 201)
                            ? request.getAbsolutePath()
                            : request.getBaseUri();
                    location = UriBuilder.fromUri(base).
                            path(locationUri.getRawPath()).
                            replaceQuery(locationUri.getRawQuery()).
                            fragment(locationUri.getRawFragment()).
                            build();
                }
                _headers.putSingle(HttpHeaders.LOCATION, location);
            }
        }

        return _headers;
    }
}
