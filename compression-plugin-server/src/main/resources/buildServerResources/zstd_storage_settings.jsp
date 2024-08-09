
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="intprop" uri="/WEB-INF/functions/intprop" %>
<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<jsp:useBean id="params" class="net.quasardb.teamcity.compression.web.ZstdParametersProvider"/>

<c:set var="compression" value="${propertiesBean.properties[params.compression]}"/>

<%--@elvariable id="availableStorages" type="java.util.List<jetbrains.buildServer.serverSide.artifacts.ArtifactStorageType>"--%>
<%--@elvariable id="newStorage" type="String"--%>
<%--@elvariable id="selectedStorageType" type="jetbrains.buildServer.serverSide.artifacts.ArtifactStorageType"--%>
<%--@elvariable id="selectedStorageName" type="String"--%>
<%--@elvariable id="storageSettingsId" type="String"--%>
<%--@elvariable id="project" type="jetbrains.buildServer.serverSide.SProject"--%>
<%--@elvariable id="publicKey" type="java.lang.String"--%>

<c:set var="canEditProject" value="${afn:permissionGrantedForProject(project, 'EDIT_PROJECT')}"/>
<c:set var="projectIsReadOnly" value="${project.readOnly}"/>

<div id="edit-zstd-storage-root">
Test page with parameters
</div>

<c:set var="storageTypes" value="${util:arrayToString(availableStorages.stream().map(it->it.getType()).toArray())}"/>
<c:set var="storageNames" value="${util:arrayToString(availableStorages.stream().map(it->it.getName()).toArray())}"/>


<script type="text/javascript">
  const config = {
    readOnly: "<bs:forJs>${projectIsReadOnly || !canEditProject}</bs:forJs>" === "true",
    storageTypes: "<bs:forJs>${storageTypes}</bs:forJs>",
    storageNames: "<bs:forJs>${storageNames}</bs:forJs>",
    containersPath: "<bs:forJs>${params.containersPath}</bs:forJs>",
    distributionPath: "<bs:forJs>${distributionPath}</bs:forJs>",
    publicKey: "<bs:forJs>${publicKey}</bs:forJs>",
    projectId: "<bs:forJs>${project.externalId}</bs:forJs>",
    isNewStorage: "<bs:forJs>${Boolean.parseBoolean(newStorage)}</bs:forJs>" === "true",
    selectedStorageType: "<bs:forJs>${selectedStorageType.type}</bs:forJs>",
    selectedStorageName: "<bs:forJs>${selectedStorageName}</bs:forJs>",
    storageSettingsId: "<bs:forJs>${storageSettingsId}</bs:forJs>",
  };
</script>
